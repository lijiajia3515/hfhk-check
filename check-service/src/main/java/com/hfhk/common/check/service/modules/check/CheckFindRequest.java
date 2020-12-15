package com.hfhk.common.check.service.modules.check;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collection;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckFindRequest {
	private Collection<String> ids;
	private String parent;
	private String sn;
	private String name;
}
