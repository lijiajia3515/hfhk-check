package com.hfhk.common.check.dist;

import com.hfhk.cairo.core.page.AbstractPage;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistCheckFindParam extends AbstractPage<DistCheckFindParam> {
	private Set<String> sns;
	private Set<String> parents;
	private String keyword;
}
