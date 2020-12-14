package com.hfhk.common.check.service.modules.system_check;

import com.hfhk.cairo.core.tree.TreeConverter;
import com.hfhk.common.check.problem.ProblemRule;
import com.hfhk.common.check.service.domain.mongo.CheckMongoV1;
import com.hfhk.common.check.service.domain.mongo.Mongo;
import com.hfhk.common.check.service.domain.mongo.ProblemMongoV1;
import com.hfhk.common.check.service.domain.mongo.SystemCheckMongoV1;
import com.hfhk.common.check.service.modules.check.CheckService;
import com.hfhk.common.check.service.modules.serialnumber.SerialNumber;
import com.hfhk.common.check.service.modules.serialnumber.StandardCheckSerialNumber;
import com.hfhk.common.check.service.modules.serialnumber.StandardProblemSerialNumber;
import com.hfhk.common.check.system.SystemCheck;
import com.hfhk.common.check.system.SystemCheckItem;
import com.hfhk.common.check.system.SystemProblem;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.hfhk.common.check.service.modules.Constants.DELIMITER;

@Service
public class SystemCheckService {
	private static final SerialNumber CHECK_SN = StandardCheckSerialNumber.INSTANCE;
	private static final SerialNumber PROBLEM_SN = StandardProblemSerialNumber.INSTANCE;
	private final MongoTemplate mongoTemplate;
	private final CheckService checkService;

	public SystemCheckService(MongoTemplate mongoTemplate, CheckService checkService) {
		this.mongoTemplate = mongoTemplate;
		this.checkService = checkService;
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
		return buildChecks(data);
	}

	@Transactional(rollbackFor = Exception.class)
	public Optional<SystemCheck> modify(SystemCheckModifyRequest request) {
		return findBySystem(request.getSystem());
	}

	// @Transactional(rollbackFor = Exception.class)
	public Optional<SystemCheck> findBySystem(String system) {
		Query query = Query.query(Criteria.where("system").is(system));
		return Optional.ofNullable(mongoTemplate.findOne(query, SystemCheckMongoV1.class, Mongo.Collection.SYSTEM_CHECK))
			.map(this::buildChecks);
	}

	public SystemCheck buildChecks(SystemCheckMongoV1 sc) {
		List<String> checkIds = sc.getItems().stream()
			.map(SystemCheckMongoV1.Item::getCheck)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		List<String> problemIds = sc.getItems().stream()
			.flatMap(x -> Optional.ofNullable(x.getProblems()).stream().flatMap(Collection::stream))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		List<CheckMongoV1> checks = Optional.of(checkIds)
			.filter(x -> !x.isEmpty())
			.map(x ->
				mongoTemplate.find(
					Query.query(Criteria.where("_id").in(x)),
					CheckMongoV1.class,
					Mongo.Collection.CHECK
				)
			)
			.orElse(Collections.emptyList());

		List<ProblemMongoV1> problems = Optional.of(problemIds)
			.filter(x -> !x.isEmpty())
			.map(x ->
				mongoTemplate.find(
					Query.query(Criteria.where("_id").in(x)),
					ProblemMongoV1.class,
					Mongo.Collection.PROBLEM
				)
			)
			.orElse(Collections.emptyList());

		return SystemCheck.builder()
			.system(sc.getSystem())
			.version(sc.getMetadata().getVersion())
			.items(buildChecks(sc.getSystem(), checks, problems))
			.build();
	}

	public List<SystemCheckItem> buildChecks(String system, Collection<CheckMongoV1> checks, Collection<ProblemMongoV1> problems) {
		List<CheckMongoV1> parents = new ArrayList<>();
		checkService.findRecursiveParent(checks.stream().map(CheckMongoV1::getParent).collect(Collectors.toSet()), parents);
		return compose(system, checks, problems, parents);
	}

	/**
	 * 组合
	 *
	 * @param checks  数据
	 * @param parents 父级数据
	 * @return 数据
	 */
	public List<SystemCheckItem> compose(String system, Collection<CheckMongoV1> checks, Collection<ProblemMongoV1> problems, List<CheckMongoV1> parents) {
		List<SystemCheckItem> items = checks.stream()
			.map(c -> {
				List<CheckMongoV1> compose = new ArrayList<>(Collections.singleton(c));
				checkService.findRecursiveParent(compose, parents);
				return compose;
			}).flatMap(compose ->
				this.checkMapper(system, compose)
					.map(x -> {
						String checkId = x.getId();
						List<SystemProblem> checkProblems = problems.stream().filter(y -> checkId.equals(y.getCheck()))
							.map(y -> problemMapper(system, y))
							.collect(Collectors.toList());
						x.setProblems(checkProblems);
						return x;
					})
					.stream()
			).collect(Collectors.toList());
		return buildCheckTree(items);
	}

	/**
	 * 构建 数据
	 *
	 * @param checks checks
	 * @return check list
	 */
	public List<SystemCheckItem> buildCheckTree(List<SystemCheckItem> checks) {
		return TreeConverter.build(checks, null, Comparator.comparing(SystemCheckItem::getSort));
	}

	/**
	 * check mapper
	 *
	 * @param compose compose
	 * @return check
	 */
	private Optional<SystemCheckItem> checkMapper(String system, List<CheckMongoV1> compose) {
		return Optional.of(compose)
			.filter(x -> !x.isEmpty())
			.map(list -> {
				CheckMongoV1 parent = list.size() > 1 ? list.get(list.size() - 2) : null;
				CheckMongoV1 last = list.get(list.size() - 1);
				return SystemCheckItem.builder()
					.id(last.getId())
					.parent(
						parent == null
							? null
							: Optional.ofNullable(parent.getSerialNumber())
							.map(CHECK_SN::encode)
							.map(x -> Stream.of(system, x).collect(Collectors.joining(CHECK_SN.getDelimiter())))
							.orElse(null)
					)
					.sn(
						Stream.concat(
							Stream.of(system),
							Stream.of(CHECK_SN.encode(last.getSerialNumber()))
						).collect(Collectors.joining(CHECK_SN.getDelimiter()))
					)
					.name(last.getName())
					.fullName(list.stream().map(CheckMongoV1::getName).collect(Collectors.joining(DELIMITER)))
					.sort(last.getMetadata().getSort())
					.build();
			});
	}

	private SystemProblem problemMapper(String system, ProblemMongoV1 data) {
		List<Long> serialNumber = Optional.ofNullable(data.getSerialNumber())
			.filter(x -> !x.isEmpty())
			.orElse(Collections.emptyList());

		String sn = Optional.of(serialNumber)
			.filter(x -> !x.isEmpty())
			.map(x -> Stream.concat(Stream.of(system), Stream.of(PROBLEM_SN.encode(x))).collect(Collectors.joining(PROBLEM_SN.getDelimiter())))
			.orElse(null);

		String checkSn = Optional.of(serialNumber)
			.filter(x -> x.size() > 1)
			.map(x -> IntStream.range(0, x.size() - 1).mapToLong(x::get).boxed().collect(Collectors.toList()))
			.map(x -> Stream.concat(Stream.of(system), Stream.of(CHECK_SN.encode(x))).collect(Collectors.joining(CHECK_SN.getDelimiter())))
			.orElse(null);

		return SystemProblem.builder()
			.id(data.getId())
			.sn(sn)
			.checkSn(checkSn)
			.title(data.getTitle())
			.provision(data.getProvision())
			.measures(data.getMeasures())
			.rules(
				data.getRules().stream()
					.map(x -> ProblemRule.builder()
						.rule(x.getRule())
						.score(x.getScore())
						.build())
					.collect(Collectors.toList())
			)
			.build();
	}
}
