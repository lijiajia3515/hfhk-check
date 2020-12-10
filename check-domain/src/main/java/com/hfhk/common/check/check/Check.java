package com.hfhk.common.check.check;

import com.hfhk.cairo.core.tree.TreeNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Check implements TreeNode<String, Check> {

	/**
	 * id
	 */
	private String id;

	/**
	 * 父级
	 */
	private String parent;

	/**
	 * 编码
	 */
	private String serialNumber;

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
	private List<String> tags;

	/**
	 * 排序值
	 */
	private Long sort;

	@Builder.Default
	private List<Check> subs = new ArrayList<>(0);

	@Override
	public String id() {
		return id;
	}

	@Override
	public String parentId() {
		return parent;
	}

	@Override
	public List<Check> subs() {
		return subs;
	}
}
