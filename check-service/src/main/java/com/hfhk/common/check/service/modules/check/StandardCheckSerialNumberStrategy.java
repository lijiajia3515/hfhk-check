package com.hfhk.common.check.service.modules.check;

import com.hfhk.common.check.service.modules.serial.AbstractSerialNumberStrategy;
import com.hfhk.common.check.service.modules.serial.SerialNumberStrategy;
import com.hfhk.common.check.service.modules.serial.SerialNumberStrategySettings;

import java.util.Arrays;

/**
 * 检查体系编码策略
 */
public class StandardCheckSerialNumberStrategy extends AbstractSerialNumberStrategy {

	private StandardCheckSerialNumberStrategy() {
		super(
			SerialNumberStrategySettings.builder()
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

	public static final StandardCheckSerialNumberStrategy INSTANCE = new StandardCheckSerialNumberStrategy();
}
