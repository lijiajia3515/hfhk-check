package com.hfhk.common.check.dist;

import com.hfhk.common.check.problem.ProblemRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistProblem implements Serializable {
	/**
	 * 编码
	 */
	private String sn;

	/**
	 * 检查SN
	 */
	private String checkSn;

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
	private List<String> provisions;

	/**
	 * 措施
	 */
	private List<String> measures;

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
