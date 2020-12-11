package com.hfhk.common.check.service.modules.problem;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.cairo.starter.web.handler.StatusResult;
import com.hfhk.common.check.problem.Problem;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import java.util.List;

@RestController
@RequestMapping("/problem")
public class ProblemApi {
	private final ProblemService problemService;

	public ProblemApi(ProblemService problemService) {
		this.problemService = problemService;
	}

	@PostMapping("/save")
	@PermitAll
	@StatusResult
	public Problem save(@RequestBody ProblemSaveRequest request) {
		return problemService.save(request).orElseThrow();
	}

	/**
	 * modify
	 *
	 * @param request request
	 * @return x
	 */
	@PostMapping("/modify")
	@PermitAll
	@StatusResult
	public Problem modify(@RequestBody ProblemModifyRequest request) {
		return problemService.modify(request).orElseThrow();
	}

	@PostMapping("/find")
	@PermitAll
	@StatusResult
	public List<Problem> find(@RequestBody ProblemFindRequest request) {
		return problemService.find(request);
	}

	@PostMapping("/find_page")
	@PermitAll
	@StatusResult
	public Page<Problem> find(@RequestBody ProblemPageFindRequest request) {
		return problemService.find(request);
	}

}
