package com.hfhk.common.check.service.modules.serialnumber;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

@Data
@Accessors(fluent = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerialNumberSettings {
	/**
	 * 分隔符
	 */
	@Builder.Default
	private String delimiter = "-";

	/**
	 * 默认策略
	 */
	@Builder.Default
	private SerialNumberStrategy defaultStrategy = SerialNumberStrategy.Number;

	/**
	 * 策略规则
	 */
	@Builder.Default
	private List<SerialNumberStrategy> strategies = Collections.emptyList();

	/**
	 * 取策略
	 *
	 * @param i index
	 * @return 策略
	 */
	public SerialNumberStrategy strategy(int i) {
		return strategies.size() > i ? strategies.get(i) : defaultStrategy;
	}
}
