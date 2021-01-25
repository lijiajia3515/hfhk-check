package com.hfhk.check.modules.check;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.cairo.core.tree.TreeConverter;
import com.hfhk.check.modules.Constants;
import com.hfhk.check.modules.serialnumber.SerialNumberService;
import com.hfhk.check.modules.serialnumber.StandardCheckSerialNumber;
import com.hfhk.check.mongo.CheckMongo;
import com.hfhk.check.mongo.Mongo;
import org.springframework.data.domain.Sort;
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
public class CheckService {
	private static final StandardCheckSerialNumber SN = StandardCheckSerialNumber.INSTANCE;
	private final MongoTemplate mongoTemplate;
	private final SerialNumberService serialNumberService;

	public CheckService(MongoTemplate mongoTemplate, SerialNumberService serialNumberService) {
		this.mongoTemplate = mongoTemplate;
		this.serialNumberService = serialNumberService;
	}

	/**
	 * save check
	 *
	 * @param param param
	 * @return check
	 */
	@Transactional(rollbackFor = Exception.class)
	public Check save(CheckSaveParam param) {
		CheckMongo parentCheck = Optional.ofNullable(param.getParent())
			.flatMap(x -> Optional.ofNullable(mongoTemplate.findById(x, CheckMongo.class, Mongo.Collection.CHECK)))
			.orElse(null);
		String parent = (parentCheck == null ? null : parentCheck.getId());
		List<Long> parentSerialNumber = (parentCheck == null ? Collections.emptyList() : parentCheck.getSerialNumber());

		long serialNumber = serialNumberService.checkGet(parent);
		CheckMongo value = CheckMongo.builder()
			.parent(parent)
			.serialNumber(Stream.concat(parentSerialNumber.stream(), Stream.of(serialNumber)).collect(Collectors.toList()))
			.name(param.getName())
			.build();
		mongoTemplate.insert(value, Mongo.Collection.CHECK);
		return buildCheck(value).orElseThrow();
	}

	/**
	 * modify check
	 *
	 * @param param param
	 * @return check
	 */
	@Transactional(rollbackFor = Exception.class)
	public Optional<Check> modify(CheckModifyParam param) {
		Query query = Query.query(Criteria.where(CheckMongo.FIELD._ID).is(param.getId()));
		Update update = Update.update(CheckMongo.FIELD.NAME, param.getName());
		CheckMongo value = mongoTemplate.findAndModify(query, update, CheckMongo.class, Mongo.Collection.CHECK);
		return Optional.ofNullable(value).flatMap(this::buildCheck);
	}

	/**
	 * find check
	 *
	 * @param param param
	 * @return check
	 */
	public List<Check> find(CheckFindParam param) {
		Criteria criteria = buildCriteria(param);
		Query query = Query.query(criteria).with(defaultSort());
		List<CheckMongo> checks = mongoTemplate.find(query, CheckMongo.class, Mongo.Collection.CHECK);
		return buildChecks(checks);
	}

	/**
	 * find page
	 *
	 * @param param param
	 * @return check
	 */
	public Page<Check> findPage(CheckFindParam param) {
		Criteria criteria = buildCriteria(param);
		Query query = Query.query(criteria);
		long total = mongoTemplate.count(query, CheckMongo.class, Mongo.Collection.CHECK);

		query.with(param.pageable()).with(defaultSort());
		List<CheckMongo> checks = mongoTemplate.find(query, CheckMongo.class, Mongo.Collection.CHECK);
		List<Check> contents = buildChecks(checks);
		return new Page<>(param, contents, total);
	}

	/**
	 * find tree
	 *
	 * @return find list
	 */
	public List<Check> findTreeAll() {
		List<CheckMongo> all = mongoTemplate.findAll(CheckMongo.class, Mongo.Collection.CHECK);
		return buildTree(all);
	}


	/**
	 * find by id
	 *
	 * @param id id
	 * @return check
	 */
	public Optional<Check> findById(String id) {
		return Optional.ofNullable(id)
			.map(x -> mongoTemplate.findById(id, CheckMongo.class, Mongo.Collection.CHECK))
			.flatMap(this::buildCheck);
	}

	/**
	 * find by serial
	 *
	 * @param sn serial number
	 * @return x
	 */
	public Optional<Check> findBySn(String sn) {
		return Optional.ofNullable(sn)
			.map(SN::decode)
			.map(x -> Query.query(Criteria.where(CheckMongo.FIELD.SERIAL_NUMBER.SELF).is(x)))
			.flatMap(query -> Optional.ofNullable(mongoTemplate.findOne(query, CheckMongo.class, Mongo.Collection.CHECK)))
			.flatMap(this::buildCheck);
	}

	private Criteria buildCriteria(CheckFindParam param) {
		Criteria criteria = new Criteria();
		Optional.ofNullable(param.getIds()).ifPresent(ids -> criteria.and(CheckMongo.FIELD._ID).in(ids));
		Optional.ofNullable(param.getParents()).ifPresent(parents -> criteria.and(CheckMongo.FIELD.PARENT).in(parents));

		Optional.of(
			Optional.ofNullable(param.getSns())
				.orElse(Collections.emptySet())
				.stream()
				.filter(Objects::nonNull)
				.map(SN::decode)
				.filter(x -> !x.isEmpty())
				.map(serialNumber -> {
					IntStream.range(0, serialNumber.size())
						.mapToObj(x -> Criteria.where(CheckMongo.FIELD.SERIAL_NUMBER.index(x)).is(serialNumber.get(x)))
						.forEach(criteria::andOperator);
					criteria.and(CheckMongo.FIELD.SERIAL_NUMBER.SELF).size(serialNumber.size() + 1);
					return criteria;
				})
				.collect(Collectors.toList()))
			.filter(x -> !x.isEmpty())
			.ifPresent(x -> criteria.orOperator(x.toArray(Criteria[]::new)));

		Optional.ofNullable(param.getName()).ifPresent(name -> criteria.and(CheckMongo.FIELD.NAME).regex(name));
		return criteria;
	}

	private Sort defaultSort() {
		return Sort.by(
			Sort.Order.asc(CheckMongo.FIELD.METADATA.SORT),
			Sort.Order.asc(CheckMongo.FIELD.SERIAL_NUMBER.SELF),
			Sort.Order.asc(CheckMongo.FIELD.METADATA.CREATED.AT),
			Sort.Order.asc(CheckMongo.FIELD.METADATA.LAST_MODIFIED.AT),
			Sort.Order.asc(CheckMongo.FIELD._ID)
		);
	}


	/**
	 * 构建 数据
	 *
	 * @param list list
	 * @return check list
	 */
	private List<Check> buildTree(List<CheckMongo> list) {
		ArrayList<CheckMongo> compose = new ArrayList<>(list);
		List<Check> checks = compose(compose, list);
		return TreeConverter.build(checks, null, Comparator.comparingInt(x -> 0));
	}

	/**
	 * find by check
	 *
	 * @param check check
	 * @return check
	 */
	private Optional<Check> buildCheck(CheckMongo check) {
		return buildChecks(Collections.singletonList(check)).stream().findAny();
	}

	public List<Check> buildChecks(List<CheckMongo> checks) {
		List<CheckMongo> parents = new ArrayList<>();
		findRecursiveParent(checks.stream().map(CheckMongo::getParent).collect(Collectors.toSet()), parents);
		return compose(checks, parents);
	}

	/**
	 * 组合
	 *
	 * @param list    数据
	 * @param parents 父级数据
	 * @return 数据
	 */
	public List<Check> compose(Collection<CheckMongo> list, List<CheckMongo> parents) {
		return list.stream()
			.map(c -> {
				List<CheckMongo> compose = new ArrayList<>(Collections.singleton(c));
				findRecursiveParent(compose, parents);
				return compose;
			}).flatMap(compose -> buildCheck(compose).stream())
			.collect(Collectors.toList());
	}

	/**
	 * check mapper
	 *
	 * @param compose compose
	 * @return check
	 */
	private Optional<Check> buildCheck(List<CheckMongo> compose) {
		return Optional.of(compose)
			.filter(x -> !x.isEmpty())
			.map(list -> {
				CheckMongo last = list.get(list.size() - 1);
				return Check.builder()
					.id(last.getId())
					.parent(last.getParent())
					.sn(SN.encode(last.getSerialNumber()))
					.name(last.getName())
					.fullName(list.stream().map(CheckMongo::getName).collect(Collectors.joining(Constants.DELIMITER)))
					.sort(last.getMetadata().getSort())
					.build();
			});
	}

	/**
	 * 递归查询父级
	 *
	 * @param ids id
	 * @param db  查询到的数据
	 */
	public void findRecursiveParent(Collection<String> ids, Collection<CheckMongo> db) {
		if (!ids.isEmpty()) {
			Query query = Query.query(Criteria.where(CheckMongo.FIELD._ID).in(ids));
			List<CheckMongo> list = mongoTemplate.find(query, CheckMongo.class, Mongo.Collection.CHECK);
			if (!list.isEmpty()) {
				db.addAll(list);
				findRecursiveParent(
					list.stream()
						.map(CheckMongo::getParent)
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
	public void findRecursiveParent(List<CheckMongo> current, Collection<CheckMongo> parents) {
		if (current.isEmpty()) return;
		Optional.ofNullable(current.get(0).getParent())
			.flatMap(id -> parents.stream().filter(c -> id.equals(c.getId())).findAny())
			.ifPresent(c -> {
				current.add(0, c);
				findRecursiveParent(current, parents);
			});
	}
}
