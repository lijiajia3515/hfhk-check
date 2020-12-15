package com.hfhk.common.check.service.modules.system_check;

import com.hfhk.cairo.starter.web.handler.StatusResult;
import com.hfhk.common.check.system.SystemCheck;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import java.util.Optional;

@RestController
@RequestMapping("/system/check")
public class SystemCheckApi {
	private final SystemCheckService systemCheckService;

	public SystemCheckApi(SystemCheckService systemCheckService) {
		this.systemCheckService = systemCheckService;
	}

	@PostMapping("/save")
	@StatusResult
	@PermitAll
	public SystemCheck save(SystemCheckSaveRequest request) {
		return systemCheckService.save(request);
	}

	@PostMapping("/modify")
	@StatusResult
	@PermitAll
	public SystemCheck modify(SystemCheckModifyRequest request) {
		return systemCheckService.modify(request).orElseThrow();
	}

	@PostMapping("/gen")
	@StatusResult
	@PermitAll
	public SystemCheck gen(String system) {
		return systemCheckService.gen(system).orElseThrow();
	}

	@PostMapping("/find_system")
	@StatusResult
	@PermitAll
	public Optional<SystemCheck> findBySystem(String system) {
		return systemCheckService.findBySystem(system);
	}

}
