package com.hfhk.common.check.service.modules.serial;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/serial")
public class SerialApi {
	private final SerialService serialService;

	public SerialApi(SerialService serialService) {
		this.serialService = serialService;
	}

	@PostMapping("/next")
	public long next(String key) {
		return serialService.next(key);
	}
}
