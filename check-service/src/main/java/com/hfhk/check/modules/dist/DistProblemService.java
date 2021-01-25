package com.hfhk.check.modules.dist;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.check.mongo.DistProblemMongo;
import com.hfhk.check.mongo.Mongo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DistProblemService {
	private final MongoTemplate mongoTemplate;

	public DistProblemService(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public List<DistProblem> find(String system, DistProblemFindParam param) {
		Criteria criteria = buildFindCriteria(system, param);
		Query query = Query.query(criteria).with(DistConstants.DEFAULT_DIST_PROBLEM_SORT);
		return mongoTemplate.find(query, DistProblemMongo.class, Mongo.Collection.DIST_PROBLEM)
			.stream()
			.map(DistConverter::distProblem)
			.collect(Collectors.toList());
	}

	public Page<DistProblem> findPage(String system, DistProblemFindParam param) {
		Criteria criteria = buildFindCriteria(system, param);
		Query query = Query.query(criteria);
		long total = mongoTemplate.count(query, DistProblemMongo.class, Mongo.Collection.DIST_PROBLEM);
		query.with(param.pageable()).with(DistConstants.DEFAULT_DIST_PROBLEM_SORT);
		List<DistProblem> contents = mongoTemplate.find(query, DistProblemMongo.class, Mongo.Collection.DIST_PROBLEM)
			.stream()
			.map(DistConverter::distProblem)
			.collect(Collectors.toList());
		return new Page<>(param, contents, total);
	}

	private Criteria buildFindCriteria(String system, DistProblemFindParam param) {
		Criteria criteria = Criteria.where(DistProblemMongo.FIELD.SYSTEM).is(system);
		Optional.ofNullable(param.getChecks()).filter(x -> !x.isEmpty())
			.ifPresent(f -> criteria.and(DistProblemMongo.FIELD.CHECK).in(f));
		Optional.of(Optional.ofNullable(param.getSns()).filter(x -> !x.isEmpty())
			.map(sns -> sns.stream().map(sn -> Criteria.where(DistProblemMongo.FIELD.SN).regex(sn)).toArray(Criteria[]::new))
			.orElse(new Criteria[0]))
			.filter(x -> x.length > 0)
			.ifPresent(criteria::orOperator);
		return criteria;
	}

}
