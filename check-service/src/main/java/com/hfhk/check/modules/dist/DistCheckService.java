package com.hfhk.check.modules.dist;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.cairo.core.tree.TreeConverter;
import com.hfhk.check.modules.serialnumber.StandardCheckSerialNumber;
import com.hfhk.check.mongo.CheckMongo;
import com.hfhk.check.mongo.DistCheckMongo;
import com.hfhk.check.mongo.Mongo;
import com.hfhk.common.check.dist.DistCheck;
import com.hfhk.common.check.dist.DistCheckFindParam;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DistCheckService {
	private static final StandardCheckSerialNumber CHECK_SN = StandardCheckSerialNumber.INSTANCE;
	private final MongoTemplate mongoTemplate;

	public DistCheckService(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public List<DistCheck> find(String system, DistCheckFindParam param) {
		Criteria criteria = buildFindCriteria(system, param);
		Query query = Query.query(criteria).with(DistConstants.DEFAULT_DIST_CHECK_SORT);
		return mongoTemplate.find(query, DistCheckMongo.class, Mongo.Collection.DIST_CHECK).stream()
			.map(check -> DistConverter.distCheck(check, Collections.emptyList()))
			.collect(Collectors.toList());
	}

	public Page<DistCheck> findPage(String system, DistCheckFindParam param) {
		Criteria criteria = buildFindCriteria(system, param);
		Query query = Query.query(criteria);
		long total = mongoTemplate.count(query, DistCheckMongo.class, Mongo.Collection.DIST);
		query.with(param.pageable()).with(DistConstants.DEFAULT_DIST_CHECK_SORT);
		List<DistCheck> contents = mongoTemplate.find(query, DistCheckMongo.class, Mongo.Collection.DIST_CHECK).stream()
			.map(check -> DistConverter.distCheck(check, Collections.emptyList()))
			.collect(Collectors.toList());
		return new Page<>(param, contents, total);
	}

	public List<DistCheck> findTree(String system, DistCheckFindParam param) {
		Criteria criteria = buildFindCriteria(system, param);
		Query query = Query.query(criteria).with(DistConstants.DEFAULT_DIST_CHECK_SORT);
		List<DistCheck> checks = mongoTemplate.find(query, DistCheckMongo.class, Mongo.Collection.DIST_CHECK).stream()
			.map(check -> DistConverter.distCheck(check, Collections.emptyList()))
			.collect(Collectors.toList());

		return TreeConverter.build(checks, null, Comparator.comparing(x -> 0));
	}

	public Criteria buildFindCriteria(@NotNull String system, @Validated DistCheckFindParam param) {
		Criteria criteria = Criteria.where(DistCheckMongo.FIELD.SYSTEM).is(system);

		Optional.ofNullable(param.getIds()).ifPresent(ids -> criteria.and(CheckMongo.FIELD._ID).in(ids));
		Optional.ofNullable(param.getParents()).ifPresent(parents -> criteria.and(CheckMongo.FIELD.PARENT).in(parents));

		Optional.of(
			Optional.ofNullable(param.getSns())
				.orElse(Collections.emptySet())
				.stream()
				.filter(Objects::nonNull)
				.map(CHECK_SN::decode)
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
}
