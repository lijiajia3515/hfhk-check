package com.hfhk.check.mongo;

import com.hfhk.cairo.mongo.data.Metadata;
import com.hfhk.cairo.mongo.data.mapping.model.AbstractMongoField;
import com.hfhk.cairo.mongo.data.mapping.model.AbstractUpperCamelCaseField;
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
public class ProblemMongo implements Serializable {
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
	 * 原数据
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


	public static final class MongoField extends AbstractUpperCamelCaseField {
		public final String CHECK = field("Check");
		public final CheckMongo.MongoField.SerialNumber SERIAL_NUMBER = new CheckMongo.MongoField.SerialNumber(this, "SerialNumber");
		public final String TITLE = field("Title");
		public final String DESCRIPTION = field("Description");
		public final String PROVISION = field("Provision");
		public final String MEASURES = field("Measures");
		public final String RULES = field("Rules");
		public final String SCORE = field("Score");


		public static final class SerialNumber extends AbstractUpperCamelCaseField {
			public SerialNumber() {
				super();
			}

			public SerialNumber(AbstractMongoField parent, String prefix) {
				super(parent, prefix);
			}

			public String index(int index) {
				return field(field("" + index));
			}
		}
	}
}
