package com.hfhk.common.check.service;

import com.hfhk.check.ServiceCheckApp;
import com.hfhk.check.modules.check.CheckService;
import com.hfhk.check.modules.dist.DistService;
import com.hfhk.check.modules.problem.ProblemService;
import com.hfhk.common.check.check.Check;
import com.hfhk.common.check.check.CheckSaveParam;
import com.hfhk.common.check.problem.Problem;
import com.hfhk.common.check.problem.ProblemFindParam;
import com.hfhk.common.check.problem.ProblemRule;
import com.hfhk.common.check.problem.ProblemSaveParam;
import com.hfhk.common.check.dist.DistSaveParam;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest(classes = ServiceCheckApp.class)
@AutoConfigureMockMvc
public class CheckMockData {

	@Autowired
	private CheckService checkService;

	@Autowired
	private ProblemService problemService;

	@Autowired
	private DistService distService;

	@Test
	public void test1() {
		final List<String> parent = Arrays.asList("安全", "质量", "行为", "设备");
		final int level = 2;
		final int size = 10;
		List<Check> list = parent.stream()
			.map(x -> CheckSaveParam.builder()
				.name(x)
				.build())
			.map(x -> checkService.save(x))
			.collect(Collectors.toList());
		List<Check> db = new ArrayList<>(list);
		rec(db, list, 0, level, size);
	}

	@Test
	public void test2() {
		List<Check> checkAll = checkService.findTreeAll();
		List<ProblemSaveParam> params = checkAll.stream()
			.flatMap(x -> x.getSubs().stream())
			.flatMap(x -> x.getSubs().stream())
			.flatMap(x -> x.getSubs().stream())
			.flatMap(c -> IntStream.range(0, 2)
				.mapToObj(i ->
					ProblemSaveParam.builder()
						.check(c.getId())
						.title("Problem-" + c.getName() + "-" + i)
						.description("Problem-Description-" + c.getName() + "-" + i)
						.provisions(Collections.singletonList("Problem-Provision-" + c.getName() + "-" + i))
						.measures(Collections.singletonList("Problem-Measures-" + c.getName() + "-" + i))
						.score(1)
						.rules(
							IntStream.range(0, 3)
								.mapToObj(x -> ProblemRule.builder().rule("Problem-Rule-" + x + "").score(BigDecimal.valueOf(x)).build())
								.collect(Collectors.toList())
						)
						.build())).collect(Collectors.toList());
		params.forEach(x -> problemService.save(x));

	}

	@Test
	public void test3() {
		List<Check> checkAll = checkService.findTreeAll();

		List<DistSaveParam.Item> itemMap = new ArrayList<>(4000);

		checkAll.forEach(c -> {
			itemMap.add(DistSaveParam.Item.builder().check(c.getId()).problems(Collections.emptyList()).build());
			c.getSubs().forEach(c1 -> {
				itemMap.add(DistSaveParam.Item.builder().check(c1.getId()).problems(Collections.emptyList()).build());
				c1.getSubs().forEach(c2 -> {
					itemMap.add(DistSaveParam.Item.builder().check(c2.getId()).problems(Collections.emptyList()).build());
					c2.getSubs().forEach(c3 -> {
						List<Problem> problems = problemService.find(ProblemFindParam.builder().checks(Collections.singleton(c3.getId())).build());
						itemMap.add(DistSaveParam.Item.builder()
							.check(c3.getId())
							.problems(problems.stream().map(Problem::getId).collect(Collectors.toList()))
							.build());
					});
				});
			});
		});

		DistSaveParam param = DistSaveParam.builder()
			.system("T01")
			.items(itemMap)
			.build();

		distService.save(param);
	}

	private void rec(List<Check> db, List<Check> current, int i, int max, int size) {
		if (i > max) return;
		List<Check> newSub = sub(current, size);
		db.addAll(newSub);
		rec(db, newSub, ++i, max, size);
	}

	private List<Check> sub(List<Check> checks, int size) {
		return IntStream.range(0, size)
			.mapToObj(x -> x + "")
			.flatMap(x -> checks.stream().map(y -> CheckSaveParam.builder().parent(y.getId()).name(x).build()))
			.map(checkService::save)
			.collect(Collectors.toList());
	}
}
