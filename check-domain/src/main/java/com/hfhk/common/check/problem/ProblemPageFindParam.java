package com.hfhk.common.check.problem;

import com.hfhk.cairo.core.page.AbstractPage;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemPageFindParam extends AbstractPage<ProblemPageFindParam> {

	/**
	 * 关键字搜索
	 */
	private String keyword;

	/**
	 * 检查
	 */
	private String check;

	/**
	 * 编码
	 */
	private String serialNumber;
}
