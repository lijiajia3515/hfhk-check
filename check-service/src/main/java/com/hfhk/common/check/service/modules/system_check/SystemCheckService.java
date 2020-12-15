package com.hfhk.common.check.service.modules.system_check;

import com.hfhk.cairo.core.tree.TreeConverter;
import com.hfhk.cairo.data.mongo.Metadata;
import com.hfhk.common.check.check.Check;
import com.hfhk.common.check.problem.Problem;
import com.hfhk.common.check.problem.ProblemRule;
import com.hfhk.common.check.service.domain.mongo.Mongo;
import com.hfhk.common.check.service.domain.mongo.SystemCheckMongoV1;
import com.hfhk.common.check.service.domain.mongo.SystemDistCheckMongoV1;
import com.hfhk.common.check.service.domain.mongo.SystemDistProblemMongoV1;
import com.hfhk.common.check.service.modules.check.CheckFindRequest;
import com.hfhk.common.check.service.modules.check.CheckService;
import com.hfhk.common.check.service.modules.problem.ProblemFindRequest;
import com.hfhk.common.check.service.modules.problem.ProblemService;
import com.hfhk.common.check.service.modules.serialnumber.SerialNumber;
import com.hfhk.common.check.service.modules.serialnumber.StandardCheckSerialNumber;
import com.hfhk.common.check.service.modules.serialnumber.StandardProblemSerialNumber;
import com.hfhk.common.check.system.SystemCheck;
import com.hfhk.common.check.system.SystemDistCheck;
import com.hfhk.common.check.system.SystemDistProblem;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class SystemCheckService {
	private static final SerialNumber CHECK_SN = StandardCheckSerialNumber.INSTANCE;
	private static final SerialNumber PROBLEM_SN = StandardProblemSerialNumber.INSTANCE;
	private final MongoTemplate mongoTemplate;
	private final CheckService checkService;
	private final ProblemService problemService;

	public SystemCheckService(MongoTemplate mongoTemplate, CheckService checkService, ProblemService problemService) {
		this.mongoTemplate = mongoTemplate;
		this.checkService = checkService;
		this.problemService = problemService;
	}

	@Transactional(rollbackFor = Exception.class)
	public SystemCheck save(SystemCheckSaveRequest request) {
		SystemCheckMongoV1 data = SystemCheckMongoV1.builder()
			.system(request.getSystem())
			.items(
				request.getItems()
					.stream()
					.map(x ->
						SystemCheckMongoV1.Item.builder()
							.check(x.getCheck())
							.problems(x.getProblems())
							.build()
					)
					.collect(Collectors.toList())
			).build();

		mongoTemplate.insert(data, Mongo.Collection.SYSTEM_CHECK);
		return gen(request.getSystem()).orElseThrow();
	}

	@Transactional(rollbackFor = Exception.class)
	public Optional<SystemCheck> modify(SystemCheckModifyRequest request) {
		return findBySystem(request.getSystem());
	}

	/**
	 * 生成 系统产物
	 *
	 * @param system system
	 * @return list system dist checks
	 */
	public Optional<SystemCheck> gen(String system) {
		Query query = Query.query(Criteria.where("metadata.deleted").is(0L).and("system").is(system));
		return Optional.ofNullable(mongoTemplate.findOne(query, SystemCheckMongoV1.class, Mongo.Collection.SYSTEM_CHECK))
			.map(x -> {
				Collection<String> checkIds = Optional.ofNullable(x.getItems())
					.map(y -> y.parallelStream()
						.map(SystemCheckMongoV1.Item::getCheck)
						.collect(Collectors.toSet()
						)
					)
					.orElse(Collections.emptySet());
				Collection<String> problemIds = Optional.ofNullable(x.getItems())
					.map(y -> y.stream()
						.map(SystemCheckMongoV1.Item::getProblems)
						.flatMap(Collection::parallelStream)
						.collect(Collectors.toSet()
						)
					)
					.orElse(Collections.emptySet());
				Map<String, Check> checkMap = Optional.of(checkIds)
					.filter(y -> !y.isEmpty())
					.map(y ->
						checkService.find(CheckFindRequest.builder().ids(y).build()).stream()
							.collect(Collectors.toMap(Check::getId, z -> z))
					).orElse(Collections.emptyMap());

				Map<String, Problem> problemMap = Optional.of(problemIds)
					.filter(y -> !y.isEmpty())
					.map(y ->
						problemService.find(ProblemFindRequest.builder().ids(y).build()).stream()
							.collect(Collectors.toMap(Problem::getId, z -> z))
					)
					.orElse(Collections.emptyMap());
				Collection<SystemDistCheckMongoV1> distChecks = new ArrayList<>();
				Collection<SystemDistProblemMongoV1> distProblems = new ArrayList<>();

				x.getItems().forEach(item -> {
					Check check = checkMap.get(item.getCheck());
					if (check != null) {
						List<Long> serialNumber = CHECK_SN.decode(check.getSn());
						List<Long> parentSerialNumber = IntStream.range(0, serialNumber.size() - 1).boxed().map(serialNumber::get).collect(Collectors.toList());
						String checkSn = Stream.of(x.getSystem(), check.getSn()).collect(Collectors.joining(CHECK_SN.getDelimiter()));
						String parentCheckSn = parentSerialNumber.isEmpty()
							? null
							: Stream.of(x.getSystem(), CHECK_SN.encode(parentSerialNumber)).collect(Collectors.joining(CHECK_SN.getDelimiter()));

						SystemDistCheckMongoV1 distCheck = SystemDistCheckMongoV1.builder()
							.system(x.getSystem())
							.sn(checkSn)
							.parent(parentCheckSn)
							.name(check.getName())
							.fullName(check.getFullName())
							.metadata(Metadata.builder().sort(check.getSort()).build())
							.build();


						List<SystemDistProblemMongoV1> distCheckProblems = item.getProblems().stream()
							.flatMap(p -> Optional.ofNullable(problemMap.get(p)).stream())
							.map(p -> {
								String problemSn = Stream.of(x.getSystem(), p.getSn()).collect(Collectors.joining(CHECK_SN.getDelimiter()));
								return SystemDistProblemMongoV1.builder()
									.system(x.getSystem())
									.sn(problemSn)
									.check(checkSn)
									.title(p.getTitle())
									.description(p.getDescription())
									.measures(p.getMeasures())
									.provision(p.getProvision())
									.rules(p.getRules().stream()
										.map(r -> SystemDistProblemMongoV1.Rule.builder()
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
				Query distDeleteQuery = Query.query(
					Criteria.where("system").is(system)
						.and("metadata.deleted").is(0L)
				);
				Update distDeleteUpdate = Update.update("metadata.deleted", com.hfhk.cairo.core.Constants.SNOWFLAKE.nextId());
				mongoTemplate.updateMulti(distDeleteQuery, distDeleteUpdate, SystemDistCheckMongoV1.class, Mongo.Collection.SYSTEM_DIST_CHECK);
				mongoTemplate.updateMulti(distDeleteQuery, distDeleteUpdate, SystemDistProblemMongoV1.class, Mongo.Collection.SYSTEM_DIST_PROBLEM);
				Collection<SystemDistCheckMongoV1> savedDistChecks = mongoTemplate.insert(distChecks, Mongo.Collection.SYSTEM_DIST_CHECK);
				Collection<SystemDistProblemMongoV1> savedDistProblems = mongoTemplate.insert(distProblems, Mongo.Collection.SYSTEM_DIST_PROBLEM);

				return buildDistCheck(x, savedDistChecks, savedDistProblems);
			});
	}

	// @Transactional(rollbackFor = Exception.class)
	public Optional<SystemCheck> findBySystem(String system) {
		Query query = Query.query(Criteria.where("metadata.deleted").is(0L).and("system").is(system));
		return Optional.ofNullable(mongoTemplate.findOne(query, SystemCheckMongoV1.class, Mongo.Collection.SYSTEM_CHECK))
			.map(sc -> {
				List<SystemDistCheckMongoV1> distChecks = mongoTemplate.find(Query.query(Criteria.where("metadata.deleted").is(0L).and("system").is(system)), SystemDistCheckMongoV1.class, Mongo.Collection.SYSTEM_DIST_CHECK);
				List<SystemDistProblemMongoV1> distProblems = mongoTemplate.find(
					Query.query(Criteria.where("metadata.deleted").is(0L).and("system").is(system)),
					SystemDistProblemMongoV1.class,
					Mongo.Collection.SYSTEM_DIST_PROBLEM);
				return buildDistCheck(sc, distChecks, distProblems);
			});
	}

	public SystemCheck buildDistCheck(SystemCheckMongoV1 sc, Collection<SystemDistCheckMongoV1> checks, Collection<SystemDistProblemMongoV1> problems) {
		List<SystemDistCheck> systemDistChecks = checks.stream()
			.map(c -> {
				List<SystemDistProblemMongoV1> checkProblems = problems.stream()
					.filter(x -> c.getSn().equals(x.getCheck()))
					.collect(Collectors.toList());
				return systemDistCheckMapper(c, checkProblems);
			})
			.collect(Collectors.toList());
		List<SystemDistCheck> content = TreeConverter.build(systemDistChecks, null, Comparator.comparing(SystemDistCheck::getSort));
		return SystemCheck.builder()
			.system(sc.getSystem())
			.version(sc.getMetadata().getVersion())
			.createdAt(sc.getMetadata().getCreated().getAt())
			.modifiedAt(sc.getMetadata().getLastModified().getAt())
			.content(content)
			.build();
	}

	private SystemDistCheck systemDistCheckMapper(SystemDistCheckMongoV1 check, List<SystemDistProblemMongoV1> problems) {
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

	public SystemDistProblem systemDistProblemMapper(SystemDistProblemMongoV1 problem) {
		return SystemDistProblem.builder()
			.sn(problem.getSn())
			.checkSn(problem.getCheck())
			.title(problem.getTitle())
			.description(problem.getDescription())
			.provision(problem.getProvision())
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
