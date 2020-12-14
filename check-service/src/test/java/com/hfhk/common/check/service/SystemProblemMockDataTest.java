package com.hfhk.common.check.service;

import com.hfhk.common.check.check.Check;
import com.hfhk.common.check.problem.Problem;
import com.hfhk.common.check.service.modules.check.CheckService;
import com.hfhk.common.check.service.modules.problem.ProblemFindRequest;
import com.hfhk.common.check.service.modules.problem.ProblemService;
import com.hfhk.common.check.service.modules.system_check.SystemCheckSaveRequest;
import com.hfhk.common.check.service.modules.system_check.SystemCheckService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = ServiceCheckApp.class)
@RunWith(SpringRunner.class)
public class SystemProblemMockDataTest {

	@Autowired
	private CheckService checkService;

	@Autowired
	private ProblemService problemService;

	@Autowired
	private SystemCheckService systemCheckService;

	@Test
	public void test1() {
		List<Check> checkAll = checkService.findTreeAll();

		List<SystemCheckSaveRequest.Item> itemMap = new ArrayList<>(4000);

		checkAll.forEach(c -> {
			itemMap.add(SystemCheckSaveRequest.Item.builder().check(c.getId()).build());
			c.getSubs().forEach(c1 -> {
				itemMap.add(SystemCheckSaveRequest.Item.builder().check(c1.getId()).build());
				c1.getSubs().forEach(c2 -> {
					itemMap.add(SystemCheckSaveRequest.Item.builder().check(c2.getId()).build());
					c2.getSubs().forEach(c3 -> {
						List<Problem> problems = problemService.find(ProblemFindRequest.builder().check(c3.getId()).build());
						itemMap.add(SystemCheckSaveRequest.Item.builder()
							.check(c3.getId())
							.problems(problems.stream().map(Problem::getId).collect(Collectors.toList()))
							.build());
					});
				});
			});
		});

		SystemCheckSaveRequest request = SystemCheckSaveRequest.builder()
			.system("T01")
			.items(itemMap)
			.build();

		systemCheckService.save(request);
	}

	@Test
	public void test2() {
		List<Problem> problems = problemService.find(ProblemFindRequest.builder().check("5fd71160dbc6466134ac3634").build());
		problems.forEach(System.out::println);
	}
}
