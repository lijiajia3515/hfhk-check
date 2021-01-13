package com.hfhk.check.modules.check;

import com.hfhk.cairo.core.page.Page;
import com.hfhk.common.check.check.Check;
import com.hfhk.common.check.check.CheckFindParam;
import com.hfhk.common.check.check.CheckModifyParam;
import com.hfhk.common.check.check.CheckSaveParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.util.List;

@RestController
@RequestMapping("/Check")
public class CheckApi {

	private final CheckService checkService;

	public CheckApi(CheckService checkService) {
		this.checkService = checkService;
	}


	@PostMapping("/Save")
	@PermitAll
	public Check save(@RequestBody(required = false) CheckSaveParam param) {
		return checkService.save(param);
	}

	@PostMapping("/Modify")
	@PermitAll
	public Check modify(@RequestBody(required = false) CheckModifyParam param) {
		return checkService.modify(param);
	}

	@PostMapping("/Find")
	@PermitAll
	public List<Check> find(@RequestBody(required = false) CheckFindParam param) {
		return checkService.find(param);
	}

	@PostMapping("/FindPage")
	@PermitAll
	public Page<Check> findPage(@RequestBody(required = false) CheckFindParam param) {
		return checkService.findPage(param);
	}

	@PostMapping("/FindTree")
	@PermitAll
	public List<Check> findTree() {
		return checkService.findTreeAll();
	}

	@GetMapping("/Find/Id/{id}")
	@PermitAll
	public Check findById(@PathVariable String id) {
		return checkService.findById(id).orElseThrow();
	}

	@GetMapping("/Find/Sn/{sn}")
	@PermitAll
	public Check findBySerial(@PathVariable String sn) {
		return checkService.findBySn(sn).orElse(null);
	}
}
