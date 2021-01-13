package com.hfhk.check.modules.problem;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.check.modules.serialnumber.SerialNumberService;
import com.hfhk.check.modules.serialnumber.StandardProblemSerialNumber;
import com.hfhk.common.check.problem.*;
import com.hfhk.check.mongo.Mongo;
import com.hfhk.check.mongo.ProblemMongo;
import com.hfhk.check.modules.check.CheckService;
import com.hfhk.check.modules.serialnumber.SerialNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j

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

	@Transactional(rollbackFor = Exception.class)
	public Problem save(@Validated ProblemSaveParam param) {
		return checkService.findById(param.getCheck())
			.map(check -> {
				long last = serialNumberService.problemGet(check.getId());
				List<Long> serialNumber = Stream.concat(SN.decode(check.getSn()).stream(), Stream.of(last))
					.collect(Collectors.toList());
				ProblemMongo problem = ProblemMongo.builder()
					.check(check.getId())
					.serialNumber(serialNumber)
					.title(param.getTitle())
					.description(param.getDescription())
					.provisions(param.getProvisions())
					.measures(param.getMeasures())
					.score(param.getScore())
					.rules(
						Optional.ofNullable(param.getRules())
							.orElse(Collections.emptyList())
							.stream()
							.map(x -> ProblemMongo.Rule.builder().rule(x.getRule()).score(x.getScore()).build())
							.collect(Collectors.toList())
					)
					.build();
				mongoTemplate.insert(problem, Mongo.Collection.PROBLEM);
				return mapper(problem);
			})
			.orElseThrow();
	}

	/**
	 * modify
	 *
	 * @param param param
	 * @return x
	 */
	@Transactional(rollbackFor = Exception.class)
	public Problem modify(@Validated ProblemModifyParam param) {
		ProblemMongo source = mongoTemplate.findById(param.getId(), ProblemMongo.class, Mongo.Collection.PROBLEM);
		return Optional.ofNullable(source)
			.map(p -> {
				if (!p.getCheck().equals(param.getCheck())) {
					// todo modify problem serialNumber
					p.setCheck(param.getCheck());
				}
				Optional.ofNullable(param.getDescription()).filter(x -> !x.equals(p.getDescription())).ifPresent(p::setDescription);
				Optional.ofNullable(param.getProvisions()).filter(x -> !x.equals(p.getProvisions())).ifPresent(p::setProvisions);
				Optional.ofNullable(param.getMeasures()).filter(x -> !x.equals(p.getMeasures())).ifPresent(p::setMeasures);
				Optional.ofNullable(param.getScore()).filter(x -> !x.equals(p.getScore())).ifPresent(p::setScore);
				// todo always modify
				Optional.ofNullable(param.getRules()).ifPresent(rules -> {
					p.setRules(rules.stream()
						.map(rule -> ProblemMongo.Rule.builder()
							.rule(rule.getRule())
							.score(rule.getScore())
							.build())
						.collect(Collectors.toList())
					);
				});
				return mongoTemplate.save(p, Mongo.Collection.PROBLEM);
			})
			.map(this::mapper)
			.orElseThrow();
	}

	@Transactional(rollbackFor = Exception.class)
	public List<Problem> delete(@Validated ProblemDeleteParam param) {
		Criteria criteria = Criteria.where(ProblemMongo.FIELD._ID).in(param.getIds());
		Query query = Query.query(criteria);
		List<Problem> deletedProblems = mongoTemplate.findAllAndRemove(query, ProblemMongo.class, Mongo.Collection.PROBLEM)
			.stream().map(this::mapper).collect(Collectors.toList());
		log.debug("[problem][delete] -> {}", deletedProblems);
		return deletedProblems;
	}

	public List<Problem> find(@Validated ProblemFindParam param) {
		Criteria criteria = buildProblemFindParamCriteria(param);
		Query query = Query.query(criteria);
		query.with(defaultSort());
		return mongoTemplate.find(query, ProblemMongo.class, Mongo.Collection.PROBLEM)
			.stream()
			.map(this::mapper)
			.collect(Collectors.toList());
	}

	public Page<Problem> findPage(@Validated ProblemFindParam param) {
		Criteria criteria = buildProblemFindParamCriteria(param);
		Query query = Query.query(criteria);
		long total = mongoTemplate.count(query, ProblemMongo.class, Mongo.Collection.PROBLEM);

		query.with(param.pageable()).with(defaultSort());
		List<Problem> contents = mongoTemplate.find(query, ProblemMongo.class, Mongo.Collection.PROBLEM)
			.stream()
			.map(this::mapper)
			.collect(Collectors.toList());
		return new Page<>(param, contents, total);
	}

	public Optional<Problem> findById(@NotNull String id) {
		return Optional.ofNullable(mongoTemplate.findById(id, ProblemMongo.class, Mongo.Collection.PROBLEM)).map(this::mapper);
	}

	public Optional<Problem> findBySn(@NotNull String sn) {
		List<Long> serialNumber = SN.decode(sn);
		Criteria criteria = Criteria.where(ProblemMongo.FIELD._ID).is(serialNumber);
		Query query = Query.query(criteria);
		return Optional.ofNullable(mongoTemplate.findOne(query, ProblemMongo.class, Mongo.Collection.PROBLEM)).map(this::mapper);
	}


	private Criteria buildProblemFindParamCriteria(@Validated ProblemFindParam param) {
		return Optional.ofNullable(param)
			.map(p -> {
				Criteria criteria = new Criteria();
				Optional.ofNullable(p.getIds()).filter(x -> !x.isEmpty()).ifPresent(ids -> criteria.and(ProblemMongo.FIELD._ID).in(ids));
				Optional.ofNullable(p.getCheck()).ifPresent(f -> criteria.and(ProblemMongo.FIELD.CHECK).in(f));
				Optional.of(
					Optional.ofNullable(p.getSn())
						.orElse(Collections.emptySet())
						.stream()
						.map(SN::decode)
						.map(serialNumber -> {
							Criteria snCriteria = Criteria.where(ProblemMongo.FIELD.SERIAL_NUMBER.SELF).size(serialNumber.size() + 1);
							Criteria[] eqCriteria = IntStream.range(0, serialNumber.size())
								.mapToObj(x -> Criteria.where(ProblemMongo.FIELD.SERIAL_NUMBER.index(x)).is(serialNumber.get(x)))
								.toArray(Criteria[]::new);
							snCriteria.andOperator(snCriteria);
							return snCriteria;
						})
						.toArray(Criteria[]::new)
				)
					.filter(x -> x.length > 0).ifPresent(criteria::orOperator);
				return criteria;
			}).orElseGet(Criteria::new);
	}

	private Sort defaultSort() {
		return Sort.by(
			Sort.Order.asc(ProblemMongo.FIELD.METADATA.SORT),
			Sort.Order.asc(ProblemMongo.FIELD.METADATA.LAST_MODIFIED.AT),
			Sort.Order.asc(ProblemMongo.FIELD.METADATA.CREATED.AT),
			Sort.Order.asc(ProblemMongo.FIELD._ID)
		);
	}

	private Problem mapper(ProblemMongo problem) {
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
