package com.hfhk.common.check.service.config;

import com.hfhk.cairo.core.exception.StatusException;
import com.hfhk.cairo.core.result.Result;
import com.hfhk.cairo.core.result.StatusResult;
import com.hfhk.cairo.core.status.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestControllerAdvice
@Configuration
public class HfhkExceptionHandler {

	@ExceptionHandler(StatusException.class)
	@ResponseStatus(HttpStatus.OK)
	public Result<Object> statusException(StatusException e, HttpServletRequest request, HttpServletResponse response) {
		e.printStackTrace();
		log.info("[Exception] url-> [{}]", request.getRequestURI());
		HttpStatus status = (e.getStatus().isSuccess()) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
		response.setStatus(status.value());
		return StatusResult.build(e.getStatus(), e.getData());
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Result<Object> runtimeException(RuntimeException e, HttpServletRequest request) {
		e.printStackTrace();
		log.info("[RuntimeException] url-> [{}]", request.getRequestURI());
		return StatusResult.buildFailed();
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Object> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request){
		e.printStackTrace();
		log.info("[HttpRequestMethodNotSupportedException] url-> [{}]", request.getRequestURI());
		return new StatusResult<>(false, Status.Failed.getCode(), e.getMessage(), e.getMethod());
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public Result<Object> exception(Exception e, HttpServletRequest request) {
		e.printStackTrace();
		log.info("[Exception] url-> [{}]", request.getRequestURI());
		return StatusResult.buildUnknown();
	}
}
