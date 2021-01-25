package com.hfhk.check.modules.dist;

import com.hfhk.cairo.core.page.AbstractPage;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistProblemFindParam extends AbstractPage<DistProblemFindParam> {

	/**
	 * 检查体系
	 */
	private Set<String> checks;

	/**
	 * 编码
	 */
	private Set<String> sns;

	/**
	 * 关键字搜索
	 */
	private String keyword;
}
