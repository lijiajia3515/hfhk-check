package com.hfhk.common.check.service.modules.serialnumber;

import java.util.List;

/**
 * 序号
 */
public interface SerialNumber {
	/**
	 * encode
	 *
	 * @param serialNumber serial number
	 * @return string serial number
	 */
	String encode(List<Long> serialNumber);

	String encode(List<Long> serialNumber, String delimiter);

	/**
	 * decoder
	 *
	 * @param serialNumber serial number
	 * @return array serial number
	 */
	List<Long> decode(String serialNumber);

	/**
	 * decode
	 *
	 * @param serialNumber serial number
	 * @param delimiter    array serial number
	 * @return array serial number
	 */
	List<Long> decode(String serialNumber, String delimiter);

}
