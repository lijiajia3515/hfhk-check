package com.hfhk.check.modules.check;

import com.hfhk.common.check.problem.ProblemRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckExcelDataParam {
	private String l1;
	private String l2;
	private String l3;
	private String l4;
	private String l5;
	private String title;
	private String description;
	private List<String> provisions;
	private List<String> measures;
	private Integer score;
	private List<ProblemRule> rules;
}
