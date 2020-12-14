package com.hfhk.common.check.service.domain.mongo;

import com.hfhk.cairo.data.mongo.Metadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@Accessors

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemMongoV1 implements Serializable {
	/**
	 * id
	 */
	private String id;

	/**
	 * 体系id
	 */
	private String check;

	/**
	 * 编码
	 */
	private List<Long> serialNumber;

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

	@Builder.Default
	private List<Rule> rules = Collections.emptyList();

	@Builder.Default
	private Integer score = 0;

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
