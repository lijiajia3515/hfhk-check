package com.hfhk.common.check.service.modules.problem;

import com.hfhk.cairo.core.request.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemPageFindRequest {
	/**
	 * 分页参数
	 */
	@Builder.Default
	private PageRequest page = new PageRequest();

	/**
	 * 关键字搜索
	 */
	private String keywords;

	/**
	 * 检查
	 */
	private String check;

	/**
	 * 编码
	 */
	private String serialNumber;
}
