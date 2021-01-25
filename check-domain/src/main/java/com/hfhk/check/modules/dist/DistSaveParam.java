package com.hfhk.check.modules.dist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Collections;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistSaveParam {

	private String system;

	@Builder.Default
	private Collection<Item> items = Collections.emptyList();

	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Item {
		/**
		 * 检查
		 */
		private String check;

		/**
		 * 问题
		 */
		@Builder.Default
		private Collection<String> problems = Collections.emptyList();
	}
}
