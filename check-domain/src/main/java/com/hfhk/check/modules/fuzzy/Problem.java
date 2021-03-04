package com.hfhk.check.modules.fuzzy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem {

	private String sn;

	private String title;

	private String remark;

	private List<Option> options;

	// 分值
	private String score;

	@Data
	@Accessors(chain = true)

	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Option {
		// 标识
		private String index;

		// 值
		private String value;

		// 检查
		private String text;

		// 得分值
		private BigDecimal score;


		private ProblemEigen eigen;
	}
}
