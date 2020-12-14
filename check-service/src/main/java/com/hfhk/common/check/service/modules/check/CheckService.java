package com.hfhk.common.check.service.modules.check;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.cairo.core.tree.TreeConverter;
import com.hfhk.common.check.check.Check;
import com.hfhk.common.check.service.domain.mongo.CheckMongoV1;
import com.hfhk.common.check.service.domain.mongo.Mongo;
import com.hfhk.common.check.service.modules.serialnumber.SerialNumber;
import com.hfhk.common.check.service.modules.serialnumber.SerialNumberService;
import com.hfhk.common.check.service.modules.serialnumber.StandardCheckSerialNumber;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.hfhk.common.check.service.modules.Constants.DELIMITER;

@Service
public class CheckService {
	private static final SerialNumber SN = StandardCheckSerialNumber.INSTANCE;
	private final MongoTemplate mongoTemplate;
	private final SerialNumberService serialNumberService;

	public CheckService(MongoTemplate mongoTemplate, SerialNumberService serialNumberService) {
		this.mongoTemplate = mongoTemplate;
		this.serialNumberService = serialNumberService;
	}

	/**
	 * save check
	 *
	 * @param request request
	 * @return check
	 */
	public Check save(CheckSaveRequest request) {
		CheckMongoV1 parentCheck = Optional.ofNullable(request.getParent()).map(parent -> mongoTemplate.findById(parent, CheckMongoV1.class, Mongo.Collection.CHECK)).orElse(null);
		String parentId = parentCheck != null ? parentCheck.getId() : null;
		List<Long> parentSerialNumber = parentCheck != null ? parentCheck.getSerialNumber() : Collections.emptyList();
		long serialNumber = serialNumberService.checkGet(parentId);
		CheckMongoV1 value = CheckMongoV1.builder()
			.parent(parentId)
			.serialNumber(Stream.concat(parentSerialNumber.stream(), Stream.of(serialNumber)).collect(Collectors.toList()))
			.name(request.getName())
			.build();
		mongoTemplate.insert(value, Mongo.Collection.CHECK);
		return findByCheck(value).orElseThrow();
	}

	/**
	 * modify check
	 *
	 * @param request request
	 * @return check
	 */
	public Check modify(CheckModifyRequest request) {
		CheckMongoV1 value = mongoTemplate.findAndModify(
			Query.query(Criteria.where(request.getId())),
			Update.update("name", request.getName()),
			CheckMongoV1.class,
			Mongo.Collection.CHECK
		);
		return Optional.ofNullable(value)
			.flatMap(this::findByCheck)
			.orElseThrow();
	}

	/**
	 * find check
	 *
	 * @param request request
	 * @return check
	 */
	public List<Check> find(CheckFindRequest request) {
		Query query = new Query();
		Criteria criteria = new Criteria();

		// optional
		Optional.ofNullable(request.getParent()).ifPresent(field -> criteria.and("parent").is(field));
		Optional.ofNullable(request.getSn())
			.map(SN::decode)
			.ifPresent(serialNumber -> {
				IntStream.range(0, serialNumber.size())
					.mapToObj(x -> Criteria.where("serialNumber." + x).is(serialNumber.get(x)))
					.forEach(criteria::andOperator);
				criteria.and("serialNumber").size(serialNumber.size() + 1);
			});
		Optional.ofNullable(request.getName()).ifPresent(field -> criteria.and("name").regex(field));

		// sort
		query.with(Sort.by(Sort.Order.asc("metadata.sort")));

		List<CheckMongoV1> checks = mongoTemplate.find(query, CheckMongoV1.class, Mongo.Collection.CHECK);
		return buildChecks(checks);
	}

	/**
	 * find page
	 *
	 * @param request request
	 * @return check
	 */
	public Page<Check> findPage(CheckPageFindRequest request) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		Optional.ofNullable(request.getParent()).ifPresent(field -> criteria.and("parent").is(field));
		Optional.ofNullable(request.getName()).ifPresent(field -> criteria.and("name").regex(field));
		long total = mongoTemplate.count(query, Mongo.Collection.CHECK);
		query.with(request.getPage().pageable());
		List<CheckMongoV1> checks = mongoTemplate.find(query, CheckMongoV1.class, Mongo.Collection.CHECK);
		List<Check> contents = buildChecks(checks);
		return new Page<>(request.getPage().pageable(), contents, total);
	}

	/**
	 * find by id
	 *
	 * @param id id
	 * @return check
	 */
	public Optional<Check> findById(String id) {
		return Optional.ofNullable(id)
			.map(x -> mongoTemplate.findById(id, CheckMongoV1.class, Mongo.Collection.CHECK))
			.flatMap(this::findByCheck);
	}

	/**
	 * find by serial
	 *
	 * @param serialNumber serial number
	 * @return x
	 */
	public Optional<Check> findBySerialNumber(String serialNumber) {
		return Optional.ofNullable(serialNumber)
			.map(SN::decode)
			.map(x -> Query.query(Criteria.where("serialNumber").is(x)))
			.map(query -> mongoTemplate.findOne(query, CheckMongoV1.class, Mongo.Collection.CHECK))
			.flatMap(this::findByCheck);
	}

	/**
	 * find tree
	 *
	 * @return find list
	 */
	public List<Check> findTreeAll() {
		List<CheckMongoV1> all = mongoTemplate.findAll(CheckMongoV1.class, Mongo.Collection.CHECK);
		return buildTree(all);
	}

	/**
	 * 组合
	 *
	 * @param list    数据
	 * @param parents 父级数据
	 * @return 数据
	 */
	public List<Check> compose(Collection<CheckMongoV1> list, List<CheckMongoV1> parents) {
		return list.stream()
			.map(c -> {
				List<CheckMongoV1> compose = new ArrayList<>(Collections.singleton(c));
				findRecursiveParent(compose, parents);
				return compose;
			}).flatMap(compose -> this.checkMapper(compose).stream())
			.collect(Collectors.toList());
	}


	/**
	 * 构建 数据
	 *
	 * @param list list
	 * @return check list
	 */
	public List<Check> buildTree(List<CheckMongoV1> list) {
		ArrayList<CheckMongoV1> compose = new ArrayList<>(list);
		List<Check> checks = compose(compose, list);
		return TreeConverter.build(checks, null, Comparator.comparingInt(x -> 0));
	}

	/**
	 * find by check
	 *
	 * @param check check
	 * @return check
	 */
	private Optional<Check> findByCheck(CheckMongoV1 check) {
		return buildChecks(Collections.singleton(check)).stream().findAny();
	}

	/**
	 * check mapper
	 *
	 * @param compose compose
	 * @return check
	 */
	private Optional<Check> checkMapper(List<CheckMongoV1> compose) {
		return Optional.of(compose)
			.filter(x -> !x.isEmpty())
			.map(list -> {
				CheckMongoV1 last = list.get(list.size() - 1);
				return Check.builder()
					.id(last.getId())
					.parent(last.getParent())
					.serialNumber(SN.encode(last.getSerialNumber()))
					.name(last.getName())
					.fullName(list.stream().map(CheckMongoV1::getName).collect(Collectors.joining(DELIMITER)))
					.sort(last.getMetadata().getSort())
					.build();
			});
	}

	public List<Check> buildChecks(Collection<CheckMongoV1> checks) {
		List<CheckMongoV1> parents = new ArrayList<>();
		findRecursiveParent(checks.stream().map(CheckMongoV1::getParent).collect(Collectors.toSet()), parents);
		return compose(checks, parents);
	}

	/**
	 * 递归查询父级
	 *
	 * @param ids id
	 * @param db  查询到的数据
	 */
	public void findRecursiveParent(Collection<String> ids, Collection<CheckMongoV1> db) {
		if (!ids.isEmpty()) {
			Query query = Query.query(Criteria.where("_id").in(ids));
			List<CheckMongoV1> list = mongoTemplate.find(query, CheckMongoV1.class, Mongo.Collection.CHECK);
			if (!list.isEmpty()) {
				db.addAll(list);
				findRecursiveParent(
					list.stream()
						.map(CheckMongoV1::getParent)
						.filter(Objects::nonNull)
						.collect(Collectors.toList()),
					db
				);
			}
		}

	}


	/**
	 * 递归查询父级
	 *
	 * @param current 当前
	 * @param parents 所有数据
	 */
	public void findRecursiveParent(List<CheckMongoV1> current, Collection<CheckMongoV1> parents) {
		if (current.isEmpty()) return;
		Optional.ofNullable(current.get(0).getParent())
			.flatMap(id -> parents.stream().filter(c -> id.equals(c.getId())).findAny())
			.ifPresent(c -> {
				current.add(0, c);
				findRecursiveParent(current, parents);
			});
	}
}
