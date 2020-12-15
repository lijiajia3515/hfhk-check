package com.hfhk.common.check.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemCheck {
	/**
	 * system
	 */
	private String system;

	/**
	 * 版本号
	 */
	@Builder.Default
	private Long version = 0L;

	private LocalDateTime createdAt;

	private LocalDateTime modifiedAt;

	@Builder.Default
	private List<SystemDistCheck> content = Collections.emptyList();
}
