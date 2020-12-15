package com.hfhk.common.check.service.modules.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collection;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemFindRequest {
	private Collection<String> ids;

	/**
	 * 关键字搜索
	 */
	private String keywords;

	/**
	 * 检查体系
	 */
	private String check;

	/**
	 * 编码
	 */
	private String sn;
}
