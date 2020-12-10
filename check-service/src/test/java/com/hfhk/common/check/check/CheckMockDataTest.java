package com.hfhk.common.check.check;

import com.hfhk.common.check.service.ServiceCheckApp;
import com.hfhk.common.check.service.modules.check.CheckSaveRequest;
import com.hfhk.common.check.service.modules.check.CheckService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest(classes = ServiceCheckApp.class)
@RunWith(SpringRunner.class)
public class CheckMockDataTest {

	@Autowired
	private CheckService checkService;

	@Test
	public void test1() {
		final List<String> parent = Arrays.asList("安全", "质量", "行为", "设备");
		final int level = 2;
		final int size = 10;
		List<Check> list = parent.stream()
			.map(x -> CheckSaveRequest.builder()
				.name(x)
				.build())
			.map(checkService::save)
			.collect(Collectors.toList());
		List<Check> db = new ArrayList<>(list);
		rec(db, list, 0, level, size);
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
			.flatMap(x -> checks.stream().map(y -> CheckSaveRequest.builder().parent(y.getId()).name(x).build()))
			.map(checkService::save)
			.collect(Collectors.toList());
	}
}
