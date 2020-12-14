package com.hfhk.common.check.service.domain.mongo;

import com.hfhk.cairo.data.mongo.Metadata;
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
public class CheckMongoV1 {
	private String id;

	@Builder.Default
	private String parent = null;

	@Builder.Default
	private List<Long> serialNumber = Collections.emptyList();

	@Builder.Default
	private String name = "";

	@Builder.Default
	private Metadata metadata = new Metadata();
}
