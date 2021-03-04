package com.hfhk.check.modules.check;

import com.hfhk.cairo.core.page.AbstractPage;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckPageFindParam extends AbstractPage<CheckPageFindParam> {
	private String parent;
	private String name;
}
