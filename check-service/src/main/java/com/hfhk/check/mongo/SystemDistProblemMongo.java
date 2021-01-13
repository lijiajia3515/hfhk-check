package com.hfhk.check.mongo;

import com.hfhk.cairo.mongo.data.Metadata;
import com.hfhk.cairo.mongo.data.mapping.model.AbstractUpperCamelCaseField;
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
public class SystemDistProblemMongo {
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
	private List<String> provisions;

	/**
	 * 措施
	 */
	private List<String> measures;

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

	public static final MongoField FIELD = new MongoField();

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

	public final static class MongoField extends AbstractUpperCamelCaseField {
		/**
		 * 系统
		 */
		public final String SYSTEM = field("System");

		/**
		 * 编码
		 */
		public final String SN = field("Sn");

		/**
		 * 体系
		 */
		public final String CHECK = field("Check");

		/**
		 * 标题
		 */
		public final String TITLE = field("Title");

		/**
		 * 描述
		 */
		public final String DESCRIPTION = field("Description");

		/**
		 * 条款
		 */
		public final String PROVISION = field("Provision");

		/**
		 * 措施
		 */
		public final String MEASURES = field("Measures");

		/**
		 * 规则
		 */
		public final String RULES = field("Rules");

		/**
		 * 分数
		 */
		private final String SCORE = field("Score");
	}
}
