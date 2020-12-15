package com.hfhk.common.check.service.modules.test;

import com.hfhk.cairo.core.exception.UnknownStatusException;
import com.hfhk.cairo.starter.web.handler.StatusResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/test")
public class TestApi {
	private final AtomicInteger i = new AtomicInteger();
	@GetMapping("/exception")
	@StatusResult
	public Object exception() throws Exception {
		switch (i.getAndIncrement() % 4){
			case 0:
				throw new UnknownStatusException("鬼晓得什么异常");
			case 1:
				int i = 1/0;
				System.out.println(i);
			case 2:
				throw new RuntimeException("程序员跑到外太空了");
			default:
				throw new Exception("哦吼");
		}
	}

}
