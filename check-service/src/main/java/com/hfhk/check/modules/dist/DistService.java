package com.hfhk.check.modules.dist;

import com.hfhk.cairo.core.CoreConstants;
import com.hfhk.cairo.core.tree.TreeConverter;
import com.hfhk.cairo.mongo.data.Metadata;
import com.hfhk.check.modules.check.Check;
import com.hfhk.check.modules.check.CheckFindParam;
import com.hfhk.check.modules.check.CheckService;
import com.hfhk.check.modules.problem.Problem;
import com.hfhk.check.modules.problem.ProblemFindParam;
import com.hfhk.check.modules.problem.ProblemService;
import com.hfhk.check.modules.serialnumber.StandardCheckSerialNumber;
import com.hfhk.check.modules.serialnumber.StandardProblemSerialNumber;
import com.hfhk.check.mongo.DistCheckMongo;
import com.hfhk.check.mongo.DistMongo;
import com.hfhk.check.mongo.DistProblemMongo;
import com.hfhk.check.mongo.Mongo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class DistService {
	private static final StandardCheckSerialNumber CHECK_SN = StandardCheckSerialNumber.INSTANCE;
	private static final StandardProblemSerialNumber PROBLEM_SN = StandardProblemSerialNumber.INSTANCE;
	private final MongoTemplate mongoTemplate;
	private final CheckService checkService;
	private final ProblemService problemService;

	public DistService(MongoTemplate mongoTemplate, CheckService checkService, ProblemService problemService) {
		this.mongoTemplate = mongoTemplate;
		this.checkService = checkService;
		this.problemService = problemService;
	}

	@Transactional(rollbackFor = Exception.class)
	public Dist save(@Validated DistSaveParam param) {
		DistMongo data = DistMongo.builder()
			.system(param.getSystem())
			.items(
				param.getItems()
					.stream()
					.map(x ->
						DistMongo.Item.builder()
							.check(x.getCheck())
							.problems(x.getProblems())
							.build()
					)
					.collect(Collectors.toList())
			).build();

		mongoTemplate.insert(data, Mongo.Collection.DIST);
		return gen(param.getSystem()).orElseThrow();
	}

	@Transactional(rollbackFor = Exception.class)
	public Optional<Dist> modify(@Validated DistModifyParam param) {
		return findBySystem(param.getSystem());
	}

	/**
	 * 生成 系统产物
	 *
	 * @param system system
	 * @return list system dist checks
	 */
	@Transactional(rollbackFor = Exception.class)
	public Optional<Dist> gen(String system) {
		Criteria criteria = Criteria
			.where(DistMongo.FIELD.METADATA.DELETED).is(0L)
			.and(DistMongo.FIELD.SYSTEM).is(system);

		Query query = Query.query(criteria);
		return Optional.ofNullable(mongoTemplate.findOne(query, DistMongo.class, Mongo.Collection.DIST))
			.map(dist -> {
				Set<String> checkIds = Optional.ofNullable(dist.getItems())
					.map(y -> y.parallelStream()
						.map(DistMongo.Item::getCheck)
						.collect(Collectors.toSet()
						)
					)
					.orElse(Collections.emptySet());
				Set<String> problemIds = Optional.ofNullable(dist.getItems())
					.map(y -> y.stream()
						.map(DistMongo.Item::getProblems)
						.flatMap(Collection::parallelStream)
						.collect(Collectors.toSet()
						)
					)
					.orElse(Collections.emptySet());
				Map<String, Check> checkMap = Optional.of(checkIds)
					.filter(y -> !y.isEmpty())
					.map(y ->
						checkService.find(CheckFindParam.builder().ids(y).build()).stream()
							.collect(Collectors.toMap(Check::getId, z -> z))
					).orElse(Collections.emptyMap());

				Map<String, Problem> problemMap = Optional.of(problemIds)
					.filter(y -> !y.isEmpty())
					.map(y ->
						problemService.find(ProblemFindParam.builder().ids(y).build()).stream()
							.collect(Collectors.toMap(Problem::getId, z -> z))
					)
					.orElse(Collections.emptyMap());
				Collection<DistCheckMongo> distChecks = new ArrayList<>();
				Collection<DistProblemMongo> distProblems = new ArrayList<>();

				dist.getItems().forEach(item -> {
					Check check = checkMap.get(item.getCheck());
					if (check != null) {
						List<Long> serialNumber = CHECK_SN.decode(check.getSn());
						List<Long> parentSerialNumber = IntStream.range(0, serialNumber.size() - 1).boxed().map(serialNumber::get).collect(Collectors.toList());
						String checkSn = Stream.of(dist.getSystem(), check.getSn()).collect(Collectors.joining(CHECK_SN.getDelimiter()));
						String parentCheckSn = parentSerialNumber.isEmpty()
							? null
							: Stream.of(dist.getSystem(), CHECK_SN.encode(parentSerialNumber)).collect(Collectors.joining(CHECK_SN.getDelimiter()));

						DistCheckMongo distCheck = DistCheckMongo.builder()
							.system(dist.getSystem())
							.sn(checkSn)
							.parent(parentCheckSn)
							.name(check.getName())
							.fullName(check.getFullName())
							.metadata(Metadata.builder().sort(check.getSort()).build())
							.build();


						List<DistProblemMongo> distCheckProblems = item.getProblems().stream()
							.flatMap(p -> Optional.ofNullable(problemMap.get(p)).stream())
							.map(p -> {
								String problemSn = Stream.of(dist.getSystem(), p.getSn()).collect(Collectors.joining(PROBLEM_SN.getDelimiter()));
								return DistProblemMongo.builder()
									.system(dist.getSystem())
									.sn(problemSn)
									.check(checkSn)
									.title(p.getTitle())
									.description(p.getDescription())
									.measures(p.getMeasures())
									.provisions(p.getProvisions())
									.rules(p.getRules().stream()
										.map(r -> DistProblemMongo.Rule.builder()
											.rule(r.getRule())
											.score(r.getScore())
											.characteristicValue(r.getCharacteristicValue())
											.build())
										.collect(Collectors.toList())
									)
									.metadata(Metadata.builder().sort(p.getSort()).build())
									.build();
							})
							.collect(Collectors.toList());
						distChecks.add(distCheck);
						distProblems.addAll(distCheckProblems);
					}
				});
				Criteria deleteCriteria = Criteria
					.where(DistMongo.FIELD.METADATA.DELETED).is(0L)
					.and(DistMongo.FIELD.SYSTEM).is(system);
				Query distDeleteQuery = Query.query(deleteCriteria);
				Update distDeleteUpdate = Update.update(DistMongo.FIELD.METADATA.DELETED, CoreConstants.SNOWFLAKE.nextId());
				mongoTemplate.updateMulti(distDeleteQuery, distDeleteUpdate, DistCheckMongo.class, Mongo.Collection.DIST_CHECK);
				mongoTemplate.updateMulti(distDeleteQuery, distDeleteUpdate, DistProblemMongo.class, Mongo.Collection.DIST_PROBLEM);
				Collection<DistCheckMongo> savedDistChecks = mongoTemplate.insert(distChecks, Mongo.Collection.DIST_CHECK);
				Collection<DistProblemMongo> savedDistProblems = mongoTemplate.insert(distProblems, Mongo.Collection.DIST_PROBLEM);

				return buildSystemDist(dist, savedDistChecks, savedDistProblems);
			});
	}

	public Optional<Dist> findBySystem(String system) {
		Criteria criteria = Criteria
			.where(DistMongo.FIELD.METADATA.DELETED).is(0L)
			.and(DistMongo.FIELD.SYSTEM).is(system);
		Query query = Query.query(criteria);
		return Optional.ofNullable(mongoTemplate.findOne(query, DistMongo.class, Mongo.Collection.DIST))
			.map(sc -> {
				Criteria checkCriteria = Criteria.where(DistCheckMongo.FIELD.METADATA.DELETED).is(0L).and(DistCheckMongo.FIELD.SYSTEM).is(system);
				Query checkQuery = Query.query(checkCriteria).with(DistConstants.DEFAULT_DIST_CHECK_SORT);
				List<DistCheckMongo> distChecks = mongoTemplate.find(checkQuery, DistCheckMongo.class, Mongo.Collection.DIST_CHECK);
				Query problemQuery = Query.query(checkCriteria).with(DistConstants.DEFAULT_DIST_PROBLEM_SORT);
				List<DistProblemMongo> distProblems = mongoTemplate.find(problemQuery, DistProblemMongo.class, Mongo.Collection.DIST_PROBLEM);
				return buildSystemDist(sc, distChecks, distProblems);
			});
	}

	/**
	 * build dist
	 *
	 * @param sd       system dist
	 * @param checks   checks
	 * @param problems problems
	 * @return system dist
	 */
	private Dist buildSystemDist(DistMongo sd, Collection<DistCheckMongo> checks, Collection<DistProblemMongo> problems) {
		List<DistCheck> systemDistChecks = checks.stream()
			.map(c -> {
				List<DistProblemMongo> checkProblems = problems.stream()
					.filter(x -> c.getSn().equals(x.getCheck()))
					.collect(Collectors.toList());
				return DistConverter.distCheck(c, checkProblems);
			})
			.collect(Collectors.toList());
		List<DistCheck> contents = TreeConverter.build(systemDistChecks, null, Comparator.comparing(DistCheck::getSort));

		return Dist.builder()
			.system(sd.getSystem())
			.version(sd.getMetadata().getVersion())
			.createdAt(sd.getMetadata().getCreated().getAt())
			.modifiedAt(sd.getMetadata().getLastModified().getAt())
			.contents(contents)
			.build();
	}


}
