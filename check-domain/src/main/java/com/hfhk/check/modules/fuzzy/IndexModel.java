package com.hfhk.check.modules.fuzzy;

import com.hfhk.cairo.core.tree.TreeNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexModel implements TreeNode<String, IndexModel> {
	// 默认排序
	public static Comparator<IndexModel> COMPARATOR = Comparator.comparing(IndexModel::getSort).thenComparing(IndexModel::getId);

	/**
	 * 标识
	 */
	private String id;

	/**
	 * 父级
	 */
	private String parent;

	/**
	 * sort
	 */
	private Long sort;

	/**
	 * 标题
	 */
	private String title;
	/**
	 * 标识
	 */
	private String sn;

	/**
	 * 子层
	 */
	private List<IndexModel> subs = new ArrayList<>(0);

	/**
	 * 权重
	 */
	private BigDecimal weight;

	@Override
	public String id() {
		return id;
	}

	@Override
	public String parent() {
		return parent;
	}

	@Override
	public List<IndexModel> subs() {
		return subs;
	}
}
