package com.hfhk.check.mongo;

import com.hfhk.cairo.domain.Metadata;
import com.hfhk.cairo.mongo.data.mapping.model.AbstractUpperCamelCaseField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors

@NoArgsConstructor
@AllArgsConstructor

@Builder
public
class SerialMongo {

	private String id;

	private String name;

	private Long value;

	private Long step;

	private String remark;

	@Builder.Default
	private Metadata metadata = new Metadata();

	public static final MongoField FIELD = new MongoField();

	public static class MongoField extends AbstractUpperCamelCaseField {
		public final String NAME = field("Name");
		public final String VALUE = field("Value");
		public final String STEP = field("Step");
		public final String Remark = field("Remark");

	}
}
