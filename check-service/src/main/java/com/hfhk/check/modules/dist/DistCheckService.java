package com.hfhk.check.modules.dist;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.cairo.core.tree.TreeConverter;
import com.hfhk.check.modules.serialnumber.StandardCheckSerialNumber;
import com.hfhk.check.mongo.DistCheckMongo;
import com.hfhk.check.mongo.DistProblemMongo;
import com.hfhk.check.mongo.Mongo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

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
		long total = mongoTemplate.count(query, DistCheckMongo.class, Mongo.Collection.DIST_CHECK);
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

		return TreeConverter.build(checks, checks.stream().findFirst().map(DistCheck::getParent).orElse(null), Comparator.comparing(x -> 0));
	}

	public Criteria buildFindCriteria(@NotNull String system, @Validated DistCheckFindParam param) {
		Criteria criteria = Criteria.where(DistCheckMongo.FIELD.SYSTEM).is(system);
		Optional.ofNullable(param.getParents()).filter(x -> !x.isEmpty())
			.ifPresent(parents -> criteria.and(DistCheckMongo.FIELD.PARENT).in(parents));
		Optional.ofNullable(param.getKeyword()).filter(x -> !x.isEmpty())
			.ifPresent(name -> criteria.and(DistCheckMongo.FIELD.NAME).regex(name));
		Optional.ofNullable(param.getSns()).filter(x -> !x.isEmpty())
			.map(sns -> sns.stream().map(sn -> Criteria.where(DistProblemMongo.FIELD.SN).regex(sn)).toArray(Criteria[]::new))
			.ifPresent(criteria::orOperator);

		return criteria;
	}
}
