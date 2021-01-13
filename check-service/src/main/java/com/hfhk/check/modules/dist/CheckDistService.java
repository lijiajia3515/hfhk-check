package com.hfhk.check.modules.dist;

import com.hfhk.cairo.core.tree.TreeConverter;
import com.hfhk.cairo.mongo.data.Metadata;
import com.hfhk.check.modules.check.CheckService;
import com.hfhk.check.modules.problem.ProblemService;
import com.hfhk.check.modules.serialnumber.SerialNumber;
import com.hfhk.check.modules.serialnumber.StandardCheckSerialNumber;
import com.hfhk.check.modules.serialnumber.StandardProblemSerialNumber;
import com.hfhk.check.mongo.Mongo;
import com.hfhk.check.mongo.SystemDistCheckMongo;
import com.hfhk.check.mongo.SystemDistMongo;
import com.hfhk.check.mongo.SystemDistProblemMongo;
import com.hfhk.common.check.check.Check;
import com.hfhk.common.check.check.CheckFindParam;
import com.hfhk.common.check.problem.Problem;
import com.hfhk.common.check.problem.ProblemFindParam;
import com.hfhk.common.check.problem.ProblemRule;
import com.hfhk.common.check.dist.*;
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
public class CheckDistService {
	private static final SerialNumber CHECK_SN = StandardCheckSerialNumber.INSTANCE;
	private static final SerialNumber PROBLEM_SN = StandardProblemSerialNumber.INSTANCE;
	private final MongoTemplate mongoTemplate;
	private final CheckService checkService;
	private final ProblemService problemService;

	public CheckDistService(MongoTemplate mongoTemplate, CheckService checkService, ProblemService problemService) {
		this.mongoTemplate = mongoTemplate;
		this.checkService = checkService;
		this.problemService = problemService;
	}

	@Transactional(rollbackFor = Exception.class)
	public CheckDist save(@Validated CheckDistSaveParam param) {
		SystemDistMongo data = SystemDistMongo.builder()
			.system(param.getSystem())
			.items(
				param.getItems()
					.stream()
					.map(x ->
						SystemDistMongo.Item.builder()
							.check(x.getCheck())
							.problems(x.getProblems())
							.build()
					)
					.collect(Collectors.toList())
			).build();

		mongoTemplate.insert(data, Mongo.Collection.SYSTEM_DIST);
		return gen(param.getSystem()).orElseThrow();
	}

	@Transactional(rollbackFor = Exception.class)
	public Optional<CheckDist> modify(@Validated CheckDistModifyParam request) {
		return findBySystem(request.getSystem());
	}

	/**
	 * 生成 系统产物
	 *
	 * @param system system
	 * @return list system dist checks
	 */
	@Transactional(rollbackFor = Exception.class)
	public Optional<CheckDist> gen(String system) {
		Criteria criteria = Criteria
			.where(SystemDistMongo.FIELD.METADATA.DELETED).is(0L)
			.and(SystemDistMongo.FIELD.SYSTEM).is(system);

		Query query = Query.query(criteria);
		return Optional.ofNullable(mongoTemplate.findOne(query, SystemDistMongo.class, Mongo.Collection.SYSTEM_DIST))
			.map(x -> {
				Set<String> checkIds = Optional.ofNullable(x.getItems())
					.map(y -> y.parallelStream()
						.map(SystemDistMongo.Item::getCheck)
						.collect(Collectors.toSet()
						)
					)
					.orElse(Collections.emptySet());
				Set<String> problemIds = Optional.ofNullable(x.getItems())
					.map(y -> y.stream()
						.map(SystemDistMongo.Item::getProblems)
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
				Collection<SystemDistCheckMongo> distChecks = new ArrayList<>();
				Collection<SystemDistProblemMongo> distProblems = new ArrayList<>();

				x.getItems().forEach(item -> {
					Check check = checkMap.get(item.getCheck());
					if (check != null) {
						List<Long> serialNumber = CHECK_SN.decode(check.getSn());
						List<Long> parentSerialNumber = IntStream.range(0, serialNumber.size() - 1).boxed().map(serialNumber::get).collect(Collectors.toList());
						String checkSn = Stream.of(x.getSystem(), check.getSn()).collect(Collectors.joining(CHECK_SN.getDelimiter()));
						String parentCheckSn = parentSerialNumber.isEmpty()
							? null
							: Stream.of(x.getSystem(), CHECK_SN.encode(parentSerialNumber)).collect(Collectors.joining(CHECK_SN.getDelimiter()));

						SystemDistCheckMongo distCheck = SystemDistCheckMongo.builder()
							.system(x.getSystem())
							.sn(checkSn)
							.parent(parentCheckSn)
							.name(check.getName())
							.fullName(check.getFullName())
							.metadata(Metadata.builder().sort(check.getSort()).build())
							.build();


						List<SystemDistProblemMongo> distCheckProblems = item.getProblems().stream()
							.flatMap(p -> Optional.ofNullable(problemMap.get(p)).stream())
							.map(p -> {
								String problemSn = Stream.of(x.getSystem(), p.getSn()).collect(Collectors.joining(PROBLEM_SN.getDelimiter()));
								return SystemDistProblemMongo.builder()
									.system(x.getSystem())
									.sn(problemSn)
									.check(checkSn)
									.title(p.getTitle())
									.description(p.getDescription())
									.measures(p.getMeasures())
									.provisions(p.getProvisions())
									.rules(p.getRules().stream()
										.map(r -> SystemDistProblemMongo.Rule.builder()
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
					.where(SystemDistMongo.FIELD.METADATA.DELETED).is(0L)
					.and(SystemDistMongo.FIELD.SYSTEM).is(system);
				Query distDeleteQuery = Query.query(deleteCriteria);
				Update distDeleteUpdate = Update.update(SystemDistMongo.FIELD.METADATA.DELETED, com.hfhk.cairo.core.Constants.SNOWFLAKE.nextId());
				mongoTemplate.updateMulti(distDeleteQuery, distDeleteUpdate, SystemDistCheckMongo.class, Mongo.Collection.SYSTEM_DIST_CHECK);
				mongoTemplate.updateMulti(distDeleteQuery, distDeleteUpdate, SystemDistProblemMongo.class, Mongo.Collection.SYSTEM_DIST_PROBLEM);
				Collection<SystemDistCheckMongo> savedDistChecks = mongoTemplate.insert(distChecks, Mongo.Collection.SYSTEM_DIST_CHECK);
				Collection<SystemDistProblemMongo> savedDistProblems = mongoTemplate.insert(distProblems, Mongo.Collection.SYSTEM_DIST_PROBLEM);

				return buildSystemDist(x, savedDistChecks, savedDistProblems);
			});
	}

	public Optional<CheckDist> findBySystem(String system) {
		Criteria criteria = Criteria
			.where(SystemDistMongo.FIELD.METADATA.DELETED).is(0L)
			.and(SystemDistMongo.FIELD.SYSTEM).is(system);
		Query query = Query.query(criteria);
		return Optional.ofNullable(mongoTemplate.findOne(query, SystemDistMongo.class, Mongo.Collection.SYSTEM_DIST))
			.map(sc -> {
				Criteria checkCriteria = Criteria.where(SystemDistCheckMongo.FIELD.METADATA.DELETED).is(0L).and(SystemDistCheckMongo.FIELD.SYSTEM).is(system);
				Query checkQuery = Query.query(checkCriteria);
				List<SystemDistCheckMongo> distChecks = mongoTemplate.find(checkQuery, SystemDistCheckMongo.class, Mongo.Collection.SYSTEM_DIST_CHECK);
				List<SystemDistProblemMongo> distProblems = mongoTemplate.find(checkQuery, SystemDistProblemMongo.class, Mongo.Collection.SYSTEM_DIST_PROBLEM);
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
	private CheckDist buildSystemDist(SystemDistMongo sd, Collection<SystemDistCheckMongo> checks, Collection<SystemDistProblemMongo> problems) {
		List<SystemDistCheck> systemDistChecks = checks.stream()
			.map(c -> {
				List<SystemDistProblemMongo> checkProblems = problems.stream()
					.filter(x -> c.getSn().equals(x.getCheck()))
					.collect(Collectors.toList());
				return systemDistCheckMapper(c, checkProblems);
			})
			.collect(Collectors.toList());
		List<SystemDistCheck> contents = TreeConverter.build(systemDistChecks, null, Comparator.comparing(SystemDistCheck::getSort));
		return CheckDist.builder()
			.system(sd.getSystem())
			.version(sd.getMetadata().getVersion())
			.createdAt(sd.getMetadata().getCreated().getAt())
			.modifiedAt(sd.getMetadata().getLastModified().getAt())
			.contents(contents)
			.build();
	}

	private SystemDistCheck systemDistCheckMapper(SystemDistCheckMongo check, List<SystemDistProblemMongo> problems) {
		return SystemDistCheck.builder()
			.sn(check.getSn())
			.parent(check.getParent())
			.name(check.getName())
			.fullName(check.getFullName())
			.tag(check.getTags())
			.sort(check.getMetadata().getSort())
			.problems(problems.stream()
				.map(this::systemDistProblemMapper)
				.collect(Collectors.toList())
			)
			.build();
	}

	private SystemDistProblem systemDistProblemMapper(SystemDistProblemMongo problem) {
		return SystemDistProblem.builder()
			.sn(problem.getSn())
			.checkSn(problem.getCheck())
			.title(problem.getTitle())
			.description(problem.getDescription())
			.provisions(problem.getProvisions())
			.measures(problem.getMeasures())
			.score(problem.getScore())
			.rules(
				problem.getRules()
					.stream().map(x -> ProblemRule.builder()
					.rule(x.getRule())
					.score(x.getScore())
					.characteristicValue(x.getCharacteristicValue())
					.build()
				)
					.collect(Collectors.toList())
			)
			.sort(problem.getMetadata().getSort())
			.build();
	}
}
