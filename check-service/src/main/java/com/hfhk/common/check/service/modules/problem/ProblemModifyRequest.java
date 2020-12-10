package com.hfhk.common.check.service.modules.problem;

import com.hfhk.common.check.problem.ProblemRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemModifyRequest {

	/**
	 * 检查体系
	 */
	private String check;

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
	private Integer score;

	/**
	 * 规则
	 */
	private List<ProblemRule> rules;
}
