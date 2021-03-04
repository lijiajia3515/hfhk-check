package com.hfhk.check.modules.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemDeleteParam {
	/**
	 * ids
	 */
	private Set<String> ids;
}
