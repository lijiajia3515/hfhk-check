package com.hfhk.common.check.service.modules.system_check;

import com.mongodb.internal.bulk.DeleteRequest;
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
public class SystemCheckModifyRequest {

	private String system;

	private List<Item> save;

	private List<DeleteRequest> delete;


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
