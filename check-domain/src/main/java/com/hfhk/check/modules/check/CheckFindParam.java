package com.hfhk.check.modules.check;

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
public class CheckFindParam extends AbstractPage<CheckFindParam> {
	private Set<String> ids;
	private Set<String> parents;
	private Set<String> sns;
	private String name;
}
