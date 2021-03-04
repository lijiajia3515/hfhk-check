package com.hfhk.check.mongo;

import com.hfhk.cairo.mongo.data.Metadata;
import com.hfhk.cairo.mongo.data.mapping.model.AbstractMongoField;
import com.hfhk.cairo.mongo.data.mapping.model.AbstractUpperCamelCaseField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;


@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckMongo {
	private String id;

	@Builder.Default
	private String parent = null;

	@Builder.Default
	private List<Long> serialNumber = Collections.emptyList();

	@Builder.Default
	private String name = "";

	@Builder.Default
	private Metadata metadata = new Metadata();

	public static final MongoField FIELD = new MongoField();

	public static final class MongoField extends AbstractUpperCamelCaseField {
		public final String PARENT = field("Parent");
		public final SerialNumber SERIAL_NUMBER = new SerialNumber(this, "SerialNumber");
		public final String NAME = field("Name");

		public static final class SerialNumber extends AbstractUpperCamelCaseField {
			public SerialNumber() {
				super();
			}

			public SerialNumber(AbstractMongoField parent, String prefix) {
				super(parent, prefix);
			}

			public String index(int index) {
				return field("" + index);
			}
		}
	}
}
