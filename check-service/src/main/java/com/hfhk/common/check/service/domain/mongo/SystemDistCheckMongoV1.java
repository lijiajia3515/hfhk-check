package com.hfhk.common.check.service.domain.mongo;

import com.hfhk.cairo.data.mongo.Metadata;
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
public class SystemDistCheckMongoV1 {
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
	private Metadata metadata;
}
