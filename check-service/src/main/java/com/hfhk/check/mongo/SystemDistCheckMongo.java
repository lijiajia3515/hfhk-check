package com.hfhk.check.mongo;

import com.hfhk.cairo.mongo.data.Metadata;
import com.hfhk.cairo.mongo.data.mapping.model.AbstractMongoField;
import com.hfhk.cairo.mongo.data.mapping.model.AbstractUpperCamelCaseField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 系统-产物-检查-v1
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemDistCheckMongo {
	/**
	 * id
	 */
	private String id;

	/**
	 * 系统
	 */
	private String system;

	/**
	 * sn
	 */
	private String sn;

	/**
	 * parent
	 */
	private String parent;

	/**
	 * name
	 */
	private String name;

	/**
	 * fullName
	 */
	private String fullName;

	/**
	 * 标签
	 */
	private List<String> tags;

	/**
	 * metadata
	 */
	@Builder.Default
	private Metadata metadata = new Metadata();

	public static final MongoField FIELD = new MongoField();

	public static final class MongoField extends AbstractUpperCamelCaseField {
		public final String SYSTEM = field("System");

		public final String SN = field("Sn");

		public final String PARENT = field("Parent");

		public final String NAME = field("Name");

		public final String FULL_NAME = field("FullName");

		public final String TAGS = field("Tags");
	}
}
