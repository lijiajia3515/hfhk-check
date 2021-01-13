package com.hfhk.check.modules.serialnumber;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 检查体系编码策略
 */
public class StandardProblemSerialNumber extends AbstractSerialNumber {
	private final String PREFIX = "P";
	private final AbstractSerialNumber checkSN = StandardCheckSerialNumber.INSTANCE;

	private StandardProblemSerialNumber() {
		super(
			SerialNumberSettings.builder()
				.defaultStrategy(SerialNumberStrategy.NUMBER3)
				.strategies(Collections.emptyList())
				.build()
		);
	}

	@Override
	public String encode(List<Long> serialNumber) {
		return String.format("%s-%s%s", checkSN.encode(serialNumber.subList(0, serialNumber.size() - 1)), PREFIX, super.encode(serialNumber.subList(serialNumber.size() - 1, serialNumber.size())));
	}

	@Override
	public List<Long> decode(String sn) {
		int delimitIndex = sn.lastIndexOf(PREFIX);
		String check = sn.substring(0, delimitIndex);
		String problem = sn.substring(delimitIndex);
		return Stream.of(checkSN.decode(check), super.decode(problem)).flatMap(Collection::stream).collect(Collectors.toList());
	}

	public static final StandardProblemSerialNumber INSTANCE = new StandardProblemSerialNumber();
}
