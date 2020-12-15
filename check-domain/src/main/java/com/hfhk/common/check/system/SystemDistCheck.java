package com.hfhk.common.check.system;

import com.hfhk.cairo.core.tree.TreeNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemDistCheck implements TreeNode<String, SystemDistCheck> {
	/**
	 * 编码
	 */
	private String sn;
	/**
	 * 父级
	 */
	private String parent;
	/**
	 * 名称
	 */
	private String name;
	/**
	 * 全名
	 */
	private String fullName;
	/**
	 * 标签
	 */
	private List<String> tag;

	/**
	 * 问题
	 */
	@Builder.Default
	private List<SystemDistProblem> problems = Collections.emptyList();

	/**
	 * 子项
	 */
	@Builder.Default
	private List<SystemDistCheck> subs = new ArrayList<>();

	/**
	 * 排序
	 */
	@Builder.Default
	private Long sort = 0L;

	@Override
	public String id() {
		return sn;
	}

	@Override
	public String parentId() {
		return parent;
	}

	@Override
	public List<SystemDistCheck> subs() {
		return subs;
	}
}
