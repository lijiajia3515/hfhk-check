package com.hfhk.common.check.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
	 * 分组
	 */
	private Integer score;

	/**
	 * 特征值
	 */
	private Integer characteristicValue;
}
