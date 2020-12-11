package com.hfhk.common.check.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor

@Builder
public class Problem {
	/**
	 * id
	 */
	private String id;
	/**
	 * 编码
	 */
	private String serialNumber;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 描述
	 */
	private String description;

	/**
	 * 条款
	 */
	private String provision;

	/**
	 * 措施
	 */
	private String measures;

	/**
	 * 分数
	 */
	@Builder.Default
	private Integer score = 0;

	@Builder.Default
	private List<ProblemRule> rules = Collections.emptyList();

	/**
	 * 排序值
	 */
	@Builder.Default
	private Long sort = 0L;


}
