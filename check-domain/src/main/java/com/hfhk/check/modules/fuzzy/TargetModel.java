package com.hfhk.check.modules.fuzzy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetModel {

	// 标识
	private String id;

	// 指标
	private String index;

	// 等级
	private String level;

	// 特征值
	private String eigenValue;

	//
	private Map<String, BigDecimal> levels;
}
