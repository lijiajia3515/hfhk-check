package com.hfhk.common.check.service.domain.mongo;

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
class SerialMongoV1 {
	private String id;
	private String name;
	private Long value;
	private Long step;
	private String remark;
}
