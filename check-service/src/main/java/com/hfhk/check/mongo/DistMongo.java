package com.hfhk.check.mongo;

import com.hfhk.cairo.mongo.data.Metadata;
import com.hfhk.cairo.mongo.data.mapping.model.AbstractUpperCamelCaseField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistMongo {
	private String id;

	private String system;

	@Builder.Default
	private List<Item> items = Collections.emptyList();

	@Builder.Default
	private Metadata metadata = new Metadata();

	public static final MongoField FIELD = new MongoField();

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

	public static final class MongoField extends AbstractUpperCamelCaseField {
		public final String SYSTEM = field("System");
		public final String ITEMS = field("Items");
	}
}
