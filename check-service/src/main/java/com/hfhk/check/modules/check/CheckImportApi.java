package com.hfhk.check.modules.check;

import com.hfhk.check.modules.dist.DistService;
import com.hfhk.check.modules.problem.ProblemService;
import com.hfhk.check.mongo.DistMongo;
import com.hfhk.check.mongo.DistProblemMongo;
import com.hfhk.check.mongo.Mongo;
import com.hfhk.check.mongo.SerialMongo;
import com.hfhk.common.check.check.Check;
import com.hfhk.common.check.check.CheckFindParam;
import com.hfhk.common.check.check.CheckSaveParam;
import com.hfhk.common.check.problem.Problem;
import com.hfhk.common.check.problem.ProblemSaveParam;
import com.hfhk.common.check.dist.Dist;
import com.hfhk.common.check.dist.DistSaveParam;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/Check/Import")
public class CheckImportApi {
	private final CheckService checkService;
	private final ProblemService problemService;
	private final DistService systemDistService;
	private final MongoTemplate mongoTemplate;

	public CheckImportApi(CheckService checkService, ProblemService problemService, DistService systemDistService, MongoTemplate mongoTemplate) {
		this.checkService = checkService;
		this.problemService = problemService;
		this.systemDistService = systemDistService;
		this.mongoTemplate = mongoTemplate;
	}

	@PostMapping
	@PermitAll
	public Dist test(@RequestBody List<CheckExcelDataParam> params) {
		String system = "YR";
		mongoTemplate.remove(Query.query(new Criteria()), Mongo.Collection.CHECK);
		mongoTemplate.remove(Query.query(new Criteria()), Mongo.Collection.PROBLEM);
		mongoTemplate.remove(Query.query(Criteria.where(DistMongo.FIELD.SYSTEM).is(system)), Mongo.Collection.DIST);
		mongoTemplate.remove(Query.query(Criteria.where(DistMongo.FIELD.SYSTEM).is(system)), Mongo.Collection.DIST_CHECK);
		mongoTemplate.remove(Query.query(Criteria.where(DistMongo.FIELD.SYSTEM).is(system)), Mongo.Collection.DIST_PROBLEM);
		List<Problem> problems = params.stream()
			.map(CheckExcelDataParam::getL1)
			.filter(Objects::nonNull)
			.distinct()
			.flatMap(l1 -> {
				CheckSaveParam l1CheckParam = CheckSaveParam.builder().parent(null).name(l1).build();
				Check l1Check = checkService.save(l1CheckParam);
				return params.stream()
					.filter(l2 -> l1.equals(l2.getL1()) && l2.getL2() != null)
					.map(CheckExcelDataParam::getL2)
					.distinct()
					.flatMap(l2 -> {
						CheckSaveParam l2CheckParam = CheckSaveParam.builder().parent(l1Check.getId()).name(l2).build();
						Check l2Check = checkService.save(l2CheckParam);
						return params.stream()
							.filter(l3 -> l1.equals(l3.getL1()) && l2.equals(l3.getL2()) && l3.getL3() != null)
							.map(CheckExcelDataParam::getL3)
							.distinct()
							.flatMap(l3 -> {
								CheckSaveParam l3CheckParam = CheckSaveParam.builder().parent(l2Check.getId()).name(l3).build();
								Check l3Check = checkService.save(l3CheckParam);
								return params.stream()
									.filter(l4 -> l1.equals(l4.getL1()) && l2.equals(l4.getL2()) && l3.equals(l4.getL3()) && l4.getL4() != null)
									.map(CheckExcelDataParam::getL4)
									.distinct()
									.flatMap(l4 -> {
										CheckSaveParam l4heckParam = CheckSaveParam.builder().parent(l3Check.getId()).name(l4).build();
										Check l4Check = checkService.save(l4heckParam);

										return params.stream()
											.filter(l5 -> l1.equals(l5.getL1()) && l2.equals(l5.getL2()) && l3.equals(l5.getL3()) && l4.equals(l5.getL4()) && l5.getL5() != null)
											.map(CheckExcelDataParam::getL5)
											.distinct()
											.flatMap(l5 -> {
												CheckSaveParam l5CheckParam = CheckSaveParam.builder().parent(l4Check.getId()).name(l5).build();
												Check l5Check = checkService.save(l5CheckParam);
												return params.stream()
													.filter(problem -> l1.equals(problem.getL1()) && l2.equals(problem.getL2()) && l3.equals(problem.getL3()) && l4.equals(problem.getL4()) && l5.equals(problem.getL5()))
													.distinct()
													.map(problem ->
														ProblemSaveParam.builder()
															.check(l5Check.getId())
															.title(problem.getTitle())
															.description(problem.getDescription())
															.provisions(problem.getProvisions())
															.measures(problem.getMeasures())
															.score(problem.getScore())
															.rules(problem.getRules())
															.build())
													.map(problemService::save);
											});
									});
							});
					});
			})
			.collect(Collectors.toList());
		problems.forEach(System.out::println);
		Map<String, Set<String>> problemMap = problems.stream().collect(Collectors.groupingBy(Problem::getCheck, Collectors.collectingAndThen(Collectors.toSet(), p -> p.stream().map(Problem::getId).collect(Collectors.toSet()))));
		List<Check> checks = checkService.find(CheckFindParam.builder().build());
		Set<DistSaveParam.Item> items = checks.stream()
			.map(x -> DistSaveParam.Item.builder().check(x.getId()).problems(problemMap.getOrDefault(x.getId(), Collections.emptySet())).build())
			.collect(Collectors.toSet());
		return systemDistService.save(DistSaveParam.builder().system("YR").items(items).build());
	}
}
