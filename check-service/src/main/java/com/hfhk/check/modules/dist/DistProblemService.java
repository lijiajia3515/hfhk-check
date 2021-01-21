package com.hfhk.check.modules.dist;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.check.modules.serialnumber.StandardProblemSerialNumber;
import com.hfhk.check.mongo.DistProblemMongo;
import com.hfhk.check.mongo.Mongo;
import com.hfhk.check.mongo.ProblemMongo;
import com.hfhk.common.check.dist.DistProblem;
import com.hfhk.common.check.dist.DistProblemFindParam;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DistProblemService {
	private static final StandardProblemSerialNumber PROBLEM_SN = StandardProblemSerialNumber.INSTANCE;
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
		Optional.ofNullable(param.getIds()).filter(x -> !x.isEmpty()).ifPresent(ids -> criteria.and(ProblemMongo.FIELD._ID).in(ids));
		Optional.ofNullable(param.getChecks()).ifPresent(f -> criteria.and(ProblemMongo.FIELD.CHECK).in(f));
		Optional.of(
			Optional.ofNullable(param.getSns())
				.orElse(Collections.emptySet())
				.stream()
				.map(PROBLEM_SN::decode)
				.map(serialNumber -> {
					Criteria snCriteria = Criteria.where(ProblemMongo.FIELD.SERIAL_NUMBER.SELF).size(serialNumber.size() + 1);
					Criteria[] eqCriteria = IntStream.range(0, serialNumber.size())
						.mapToObj(x -> Criteria.where(ProblemMongo.FIELD.SERIAL_NUMBER.index(x)).is(serialNumber.get(x)))
						.toArray(Criteria[]::new);
					snCriteria.andOperator(eqCriteria);
					return snCriteria;
				})
				.toArray(Criteria[]::new))
			.filter(x -> x.length > 0)
			.ifPresent(criteria::orOperator);
		return criteria;
	}

}
