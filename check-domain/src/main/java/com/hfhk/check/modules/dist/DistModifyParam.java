package com.hfhk.check.modules.dist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistModifyParam {

	private String system;

	private List<Item> save;

	private List<Item> delete;


	@Data
	@Accessors(chain = true)

	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Item {
		private String check;
		private List<String> problems;
	}

}
