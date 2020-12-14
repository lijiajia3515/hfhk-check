package com.hfhk.common.check.service.domain.mongo;

import com.hfhk.cairo.data.mongo.Metadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemCheckMongoV1 {
	private String id;

	private String system;

	@Builder.Default
	private List<Item> items = Collections.emptyList();

	@Builder.Default
	private Metadata metadata = new Metadata();

	@Data
	@Accessors(chain = true)

	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Item {
		private String check;

		@Builder.Default
		private Collection<String> problems = Collections.emptyList();
	}
}
