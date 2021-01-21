package com.hfhk.check.modules.dist;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.common.check.dist.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Dist")
public class DistApi {
	private final DistService distService;
	private final DistCheckService distCheckService;
	private final DistProblemService distProblemService;

	public DistApi(DistService distService, DistCheckService distCheckService, DistProblemService distProblemService) {
		this.distService = distService;
		this.distCheckService = distCheckService;
		this.distProblemService = distProblemService;
	}

	@PostMapping("/Save")
	@PermitAll
	public Dist save(DistSaveParam param) {
		return distService.save(param);
	}

	@PutMapping("/Modify")
	@PermitAll
	public Dist modify(DistModifyParam param) {
		return distService.modify(param).orElseThrow();
	}

	@PostMapping("/Gen")
	@PermitAll
	public Dist gen(String system) {
		return distService.gen(system).orElseThrow();
	}

	@GetMapping("/{system}")
	@PermitAll
	public Optional<Dist> system(@PathVariable String system) {
		return distService.findBySystem(system);
	}

	@PostMapping("/{system}/Check/Find")
	public List<DistCheck> findDistCheck(@PathVariable String system, @RequestBody DistCheckFindParam param) {
		return distCheckService.find(system, param);
	}

	@PostMapping("/{system}/Check/FindPage")
	public Page<DistCheck> findPageDistCheck(@PathVariable String system, @RequestBody DistCheckFindParam param) {
		return distCheckService.findPage(system, param);
	}

	@PostMapping("/{system}/Check/FindTree")
	public List<DistCheck> findTreeSystemCheck(@PathVariable String system, @RequestBody DistCheckFindParam param) {
		return distCheckService.findTree(system, param);
	}

	@PostMapping("/{system}/Problem/Find")
	public List<DistProblem> findDistProblem(@PathVariable String system, @RequestBody DistProblemFindParam param) {
		return distProblemService.find(system, param);
	}

	@PostMapping("/{system}/Problem/FindPage")
	public Page<DistProblem> findPageDistProblem(@PathVariable String system, @RequestBody DistProblemFindParam param) {
		return distProblemService.findPage(system, param);
	}

}
