package com.hfhk.common.check.service.config;

import com.hfhk.cairo.core.exception.StatusException;
import com.hfhk.cairo.core.result.Result;
import com.hfhk.cairo.core.result.StatusResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
@Configuration
public class HfhkExceptionHandler {

	@ExceptionHandler(StatusException.class)
	public Result<Object> statusException(StatusException e, HttpServletRequest request) {
		log.debug("[Exception] url-> [{}] AuthType -> [{}]", request.getRequestURI(), request.getAuthType());
		return StatusResult.build(e.getStatus(), e.getData());
	}

	@ExceptionHandler(RuntimeException.class)
	public Result<Object> runtimeException(RuntimeException e, HttpServletRequest request) {
		log.debug("[RuntimeException] url-> [{}] AuthType -> [{}]", request.getRequestURI(), request.getAuthType());
		return StatusResult.buildFailed();
	}
	@ExceptionHandler(Exception.class)
	public Result<Object> exception(Exception e, HttpServletRequest request) {
		log.debug("[Exception] url-> [{}] AuthType -> [{}]", request.getRequestURI(), request.getAuthType());
		return StatusResult.buildUnknown();
	}
}
