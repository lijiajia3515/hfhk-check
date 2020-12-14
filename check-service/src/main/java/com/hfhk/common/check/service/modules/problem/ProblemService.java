package com.hfhk.common.check.service.modules.problem;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.common.check.problem.Problem;
import com.hfhk.common.check.problem.ProblemRule;
import com.hfhk.common.check.service.domain.mongo.Mongo;
import com.hfhk.common.check.service.domain.mongo.ProblemMongoV1;
import com.hfhk.common.check.service.modules.check.CheckService;
import com.hfhk.common.check.service.modules.serialnumber.SerialNumber;
import com.hfhk.common.check.service.modules.serialnumber.SerialNumberService;
import com.hfhk.common.check.service.modules.serialnumber.StandardProblemSerialNumber;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class ProblemService {
	public static final SerialNumber SN = StandardProblemSerialNumber.INSTANCE;
	private final SerialNumberService serialNumberService;
	private final CheckService checkService;
	private final MongoTemplate mongoTemplate;

	public ProblemService(SerialNumberService serialNumberService, CheckService checkService, MongoTemplate mongoTemplate) {
		this.serialNumberService = serialNumberService;
		this.checkService = checkService;
		this.mongoTemplate = mongoTemplate;
	}

	public Optional<Problem> save(ProblemSaveRequest request) {
		return checkService.findById(request.getCheck())
			.map(check -> {
				long last = serialNumberService.problemGet(check.getId());
				List<Long> serialNumber = Stream.concat(SN.decode(check.getSerialNumber()).stream(), Stream.of(last))
					.collect(Collectors.toList());
				ProblemMongoV1 problem = ProblemMongoV1.builder()
					.check(check.getParent())
					.serialNumber(serialNumber)
					.title(request.getTitle())
					.description(request.getDescription())
					.provision(request.getProvision())
					.measures(request.getMeasures())
					.score(request.getScore())
					.rules(
						Optional.ofNullable(request.getRules())
							.orElse(Collections.emptyList())
							.stream()
							.map(x -> ProblemMongoV1.Rule.builder().rule(x.getRule()).score(x.getScore()).build())
							.collect(Collectors.toList())
					)
					.build();
				mongoTemplate.insert(problem, Mongo.Collection.PROBLEM);
				return mapper(problem);
			});
	}

	/**
	 * modify
	 *
	 * @param request request
	 * @return x
	 */
	public Optional<Problem> modify(ProblemModifyRequest request) {
		ProblemMongoV1 source = mongoTemplate.findById(request.getId(), ProblemMongoV1.class, Mongo.Collection.PROBLEM);
		return Optional.ofNullable(source)
			.map(p -> {
				if (!p.getCheck().equals(request.getCheck())) {
					// todo modify problem serialNumber
					p.setCheck(request.getCheck());
				}
				Optional.ofNullable(request.getDescription()).filter(x -> !x.equals(p.getDescription())).ifPresent(p::setDescription);
				Optional.ofNullable(request.getProvision()).filter(x -> !x.equals(p.getProvision())).ifPresent(p::setProvision);
				Optional.ofNullable(request.getMeasures()).filter(x -> !x.equals(p.getMeasures())).ifPresent(p::setMeasures);
				Optional.ofNullable(request.getScore()).filter(x -> !x.equals(p.getScore())).ifPresent(p::setScore);
				// todo always modify
				Optional.ofNullable(request.getRules()).ifPresent(rules -> {
					p.setRules(rules.stream()
						.map(rule -> ProblemMongoV1.Rule.builder()
							.rule(rule.getRule())
							.score(rule.getScore())
							.build())
						.collect(Collectors.toList())
					);
				});
				return mongoTemplate.save(p, Mongo.Collection.PROBLEM);
			})
			.map(this::mapper);
	}

	public List<Problem> find(ProblemFindRequest request) {
		Query query = Query.query(
			Optional.ofNullable(request)
				.map(p -> {
					Criteria c = new Criteria();
					Optional.ofNullable(p.getCheck()).ifPresent(f -> c.and("check").is(f));
					Optional.ofNullable(p.getSn())
						.map(SN::decode)
						.ifPresent(serialNumber -> {
							IntStream.range(0, serialNumber.size())
								.mapToObj(x -> Criteria.where("serialNumber." + x).is(serialNumber.get(x)))
								.forEach(c::andOperator);
							c.and("serialNumber").size(serialNumber.size() + 1);
						});
					return c;
				}).orElseGet(Criteria::new)
		);
		query.with(Sort.by(Sort.Order.desc("metadata.sort"), Sort.Order.desc("metadata.created.at")));
		return mongoTemplate.find(query, ProblemMongoV1.class, Mongo.Collection.PROBLEM)
			.stream()
			.map(this::mapper)
			.collect(Collectors.toList());
	}

	public Page<Problem> find(ProblemPageFindRequest request) {
		Criteria criteria = new Criteria();
		Optional.ofNullable(request.getCheck()).ifPresent(f -> criteria.and("check").is(f));
		Optional.ofNullable(request.getSerialNumber())
			.map(SN::decode)
			.ifPresent(serialNumber -> {
				IntStream.range(0, serialNumber.size())
					.mapToObj(x -> Criteria.where("serialNumber." + x).is(serialNumber.get(x)))
					.forEach(criteria::andOperator);
				criteria.and("serialNumber").size(serialNumber.size() + 1);
			});
		Query query = Query.query(criteria);
		long total = mongoTemplate.count(query, ProblemMongoV1.class, Mongo.Collection.PROBLEM);

		query.with(request.getPage().pageable());
		query.with(Sort.by(Sort.Order.desc("metadata.sort"), Sort.Order.desc("metadata.created.at")));
		List<Problem> contents = mongoTemplate.find(query, ProblemMongoV1.class, Mongo.Collection.PROBLEM)
			.stream()
			.map(this::mapper)
			.collect(Collectors.toList());
		return new Page<>(request.getPage().pageable(), contents, total);
	}

	private Problem mapper(ProblemMongoV1 problem) {
		return Problem.builder()
			.id(problem.getId())
			.sn(SN.encode(problem.getSerialNumber()))
			.title(problem.getTitle())
			.description(problem.getDescription())
			.score(problem.getScore())
			.measures(problem.getMeasures())
			.rules(
				Optional.ofNullable(problem.getRules())
					.orElse(Collections.emptyList())
					.stream()
					.map(x -> ProblemRule.builder()
						.rule(x.getRule())
						.score(x.getScore())
						.build()
					)
					.collect(Collectors.toList())
			)
			.build();
	}

}
