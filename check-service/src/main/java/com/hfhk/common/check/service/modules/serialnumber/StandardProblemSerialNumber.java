package com.hfhk.common.check.service.modules.serialnumber;

import java.util.Arrays;

/**
 * 检查体系编码策略
 */
public class StandardProblemSerialNumber extends AbstractSerialNumber {

	private StandardProblemSerialNumber() {
		super(
			SerialNumberSettings.builder()
				.defaultStrategy(SerialNumberStrategy.NUMBER3)
				.strategies(
					Arrays.asList(
						SerialNumberStrategy.BASE_22,
						SerialNumberStrategy.NUMBER3,
						SerialNumberStrategy.NUMBER3,
						SerialNumberStrategy.NUMBER3,
						SerialNumberStrategy.PROBLEM_NUMBER3
						)
				)
				.build()
		);
	}

	public static final StandardProblemSerialNumber INSTANCE = new StandardProblemSerialNumber();
}
