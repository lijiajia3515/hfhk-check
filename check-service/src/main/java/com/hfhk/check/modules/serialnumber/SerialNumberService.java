package com.hfhk.check.modules.serialnumber;

import com.hfhk.check.modules.serial.SerialService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SerialNumberService {
	private static final String SERIAL_KEY = "SerialNumber";
	private final SerialService serialService;

	public SerialNumberService(SerialService serialService) {
		this.serialService = serialService;
	}

	/**
	 * 取号
	 *
	 * @param id id
	 * @return 号
	 */
	public long checkGet(String id) {
		String key = String.format(SERIAL_KEY.concat("-Check-%s"), Optional.ofNullable(id).orElse("Default"));
		return serialService.next(key);
	}

	public long problemGet(String id) {
		String key = String.format(SERIAL_KEY.concat("-Problem-%s"), Optional.ofNullable(id).orElse("Default"));
		return serialService.next(key);
	}

}
