package com.hfhk.check.modules.problem;

import com.hfhk.cairo.core.page.AbstractPage;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemFindParam extends AbstractPage<ProblemFindParam> {
	private Collection<String> ids;

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
