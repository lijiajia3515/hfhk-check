package com.hfhk.common.check.service.modules.check;

import com.hfhk.cairo.core.request.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckPageFindRequest {
	@Builder.Default
	private PageRequest page = new PageRequest();
	private String parent;
	private String name;
}
