package com.hfhk.common.check.service.domain.mongo;

import com.hfhk.cairo.mongo.data.Metadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * 系统-产物-问题库
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemDistProblemMongoV1 {
	/**
	 * 标识
	 */
	private String id;

	/**
	 * 系统
	 */
	private String system;

	/**
	 * 编码
	 */
	private String sn;

	/**
	 * 体系
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
	 * 规则
	 */
	@Builder.Default
	private List<Rule> rules = Collections.emptyList();

	/**
	 * 分数
	 */
	@Builder.Default
	private Integer score = 0;

	/**
	 * metadata
	 */
	@Builder.Default
	private Metadata metadata = new Metadata();

	/**
	 * 规则
	 */
	@Data
	@Accessors

	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Rule {
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
}
