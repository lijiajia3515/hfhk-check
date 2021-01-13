package com.hfhk.check.modules.dist;

import com.hfhk.common.check.dist.CheckDist;
import com.hfhk.common.check.dist.CheckDistModifyParam;
import com.hfhk.common.check.dist.CheckDistSaveParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import java.util.Optional;

@RestController
@RequestMapping("/Dist")
public class CheckDistApi {
	private final CheckDistService distService;

	public CheckDistApi(CheckDistService distService) {
		this.distService = distService;
	}

	@PostMapping("/Save")
	@PermitAll
	public CheckDist save(CheckDistSaveParam param) {
		return distService.save(param);
	}

	@PostMapping("/Modify")
	@PermitAll
	public CheckDist modify(CheckDistModifyParam param) {
		return distService.modify(param).orElseThrow();
	}

	@PostMapping("/Gen")
	@PermitAll
	public CheckDist gen(String system) {
		return distService.gen(system).orElseThrow();
	}

	@PostMapping("/FindSystem")
	@PermitAll
	public Optional<CheckDist> findBySystem(String system) {
		return distService.findBySystem(system);
	}

}
