package com.hfhk.check.modules.problem;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.common.check.problem.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Problem")
public class ProblemApi {
	private final ProblemService problemService;

	public ProblemApi(ProblemService problemService) {
		this.problemService = problemService;
	}

	@PostMapping("/Save")
	@PermitAll
	public Problem save(@RequestBody ProblemSaveParam param) {
		return problemService.save(param);
	}

	/**
	 * modify
	 *
	 * @param param param
	 * @return problem
	 */
	@PostMapping("/Modify")
	@PermitAll
	public Problem modify(@RequestBody ProblemModifyParam param) {
		return problemService.modify(param);
	}

	@DeleteMapping("/Delete")
	@PermitAll
	public List<Problem> delete(@RequestBody ProblemDeleteParam param) {
		return problemService.delete(param);
	}

	@PostMapping("/Find")
	@PermitAll
	public List<Problem> find(@RequestBody ProblemFindParam param) {
		return problemService.find(param);
	}

	@PostMapping("/FindPage")
	@PermitAll
	public Page<Problem> findPage(@RequestBody ProblemFindParam param) {
		return problemService.findPage(param);
	}

	@GetMapping("/Find/Id/{id}")
	@PermitAll
	public Optional<Problem> findById(@PathVariable String id) {
		return problemService.findById(id);
	}

	@GetMapping("/Find/Sn/{sn}")
	@PermitAll
	public Optional<Problem> findBySn(@PathVariable String sn) {
		return problemService.findBySn(sn);
	}

}
