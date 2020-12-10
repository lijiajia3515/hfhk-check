package com.hfhk.common.check.service.modules.check;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.cairo.starter.web.handler.StatusResult;
import com.hfhk.common.check.check.Check;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import java.util.List;

@RestController
@RequestMapping("/check")
public class CheckApi {
	private final CheckService checkService;

	public CheckApi(CheckService checkService) {
		this.checkService = checkService;
	}


	@PostMapping("/save")
	@StatusResult
	@PermitAll
	public Check save(@RequestBody(required = false) CheckSaveRequest request) {
		return checkService.save(request);
	}

	@PostMapping("/modify")
	@StatusResult
	@PermitAll
	public Check modify(@RequestBody(required = false) CheckModifyRequest request) {
		return checkService.modify(request);
	}

	@PostMapping("/find")
	@StatusResult
	@PermitAll
	public List<Check> find(@RequestBody(required = false) CheckFindRequest request) {
		return checkService.find(request);
	}

	@PostMapping("/find_tree")
	@StatusResult
	@PermitAll
	public List<Check> tree() {
		return checkService.findTreeAll();
	}

	@PostMapping("/find_page")
	@StatusResult
	@PermitAll
	public Page<Check> pageFind(@RequestBody(required = false) CheckPageFindRequest request) {
		return checkService.findPage(request);
	}
}
