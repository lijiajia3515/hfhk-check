package com.hfhk.check.modules.dist;

import com.hfhk.check.mongo.DistCheckMongo;
import com.hfhk.check.mongo.DistProblemMongo;
import com.hfhk.common.check.dist.DistCheck;
import com.hfhk.common.check.dist.DistProblem;
import com.hfhk.common.check.problem.ProblemRule;

import java.util.List;
import java.util.stream.Collectors;

public class DistConverter {
	public static DistCheck distCheck(DistCheckMongo check, List<DistProblemMongo> problems) {
		return DistCheck.builder()
			.sn(check.getSn())
			.parent(check.getParent())
			.name(check.getName())
			.fullName(check.getFullName())
			.tag(check.getTags())
			.sort(check.getMetadata().getSort())
			.problems(problems.stream()
				.map(DistConverter::distProblem)
				.collect(Collectors.toList())
			)
			.build();
	}

	public static DistProblem distProblem(DistProblemMongo problem) {
		return DistProblem.builder()
			.sn(problem.getSn())
			.check(problem.getCheck())
			.title(problem.getTitle())
			.description(problem.getDescription())
			.provisions(problem.getProvisions())
			.measures(problem.getMeasures())
			.score(problem.getScore())
			.rules(
				problem.getRules()
					.stream()
					.map(x ->
						ProblemRule.builder()
							.rule(x.getRule())
							.score(x.getScore())
							.characteristicValue(x.getCharacteristicValue())
							.build()
					)
					.collect(Collectors.toList())
			)
			.sort(problem.getMetadata().getSort())
			.build();
	}
}
