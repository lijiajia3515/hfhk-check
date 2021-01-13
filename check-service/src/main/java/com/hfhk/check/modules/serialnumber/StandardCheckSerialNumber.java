package com.hfhk.check.modules.serialnumber;

import java.util.Arrays;
import java.util.List;

/**
 * 检查体系编码策略
 */
public class StandardCheckSerialNumber extends AbstractSerialNumber {


	public static final StandardCheckSerialNumber INSTANCE = new StandardCheckSerialNumber();

	private StandardCheckSerialNumber() {
		super(
			SerialNumberSettings.builder()
				.defaultStrategy(SerialNumberStrategy.NUMBER3)
				.strategies(
					Arrays.asList(
						SerialNumberStrategy.BASE_22,
						SerialNumberStrategy.NUMBER3,
						SerialNumberStrategy.NUMBER3,
						SerialNumberStrategy.NUMBER3
					)
				)
				.build()
		);
	}

	@Override
	public String encode(List<Long> serialNumber) {
		return super.encode(serialNumber);
	}

	@Override
	public List<Long> decode(String sn) {
		return super.decode(sn);
	}
}
