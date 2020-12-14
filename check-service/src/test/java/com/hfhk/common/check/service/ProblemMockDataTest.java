package com.hfhk.common.check.service;

import com.hfhk.common.check.check.Check;
import com.hfhk.common.check.problem.ProblemRule;
import com.hfhk.common.check.service.modules.check.CheckService;
import com.hfhk.common.check.service.modules.problem.ProblemSaveRequest;
import com.hfhk.common.check.service.modules.problem.ProblemService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest(classes = ServiceCheckApp.class)
@RunWith(SpringRunner.class)
public class ProblemMockDataTest {

	@Autowired
	private CheckService checkService;

	@Autowired
	private ProblemService problemService;

	@Test
	public void test1() {
		List<Check> checkAll = checkService.findTreeAll();
		List<ProblemSaveRequest> requests = checkAll.stream()
			.flatMap(x -> x.getSubs().stream())
			.flatMap(x -> x.getSubs().stream())
			.flatMap(x -> x.getSubs().stream())
			.flatMap(c -> IntStream.range(0, 1)
				.mapToObj(i ->
					ProblemSaveRequest.builder()
						.check(c.getId())
						.title("Problem-" + c.getName() + "-" + i)
						.description("Problem-Description-" + c.getName() + "-" + i)
						.provision("Problem-Provision-" + c.getName() + "-" + i)
						.measures("Problem-Measures-" + c.getName() + "-" + i)
						.score(1)
						.rules(
							IntStream.range(0, 3)
								.mapToObj(x -> ProblemRule.builder().rule("Problem-Rule-" + x+"").score(x).build())
								.collect(Collectors.toList())
						)
						.build())).collect(Collectors.toList());
		requests.forEach(x -> problemService.save(x));

	}
}
