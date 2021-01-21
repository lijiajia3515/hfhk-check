package com.hfhk.common.check.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 问题-规则
 */
@Data
@Accessors

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemRule {

	/**
	 * 规则
	 */
	private String rule;

	/**
	 * 分数
	 */
	private BigDecimal score;

	/**
	 * 特征值
	 */
	private Integer characteristicValue;
}
