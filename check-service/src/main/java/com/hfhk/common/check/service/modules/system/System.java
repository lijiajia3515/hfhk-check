package com.hfhk.common.check.service.modules.system;

public enum System {
	/**
	 * 东湖高新-建设局
	 */
	DHGXConstructionBureau("DGGX_JSJ"),
	/**
	 * 建丰-监理
	 */
	JianFengSupervison("JFJL"),
	/**
	 * 毅瑞-第三方检查
	 */
	YiRuiThirdCheck("YR"),
	T01("T01"),
	T02("T02"),
	T03("T03"),
	T04("T04"),
	T05("T05"),
	T06("T06"),
	T07("T07");
	/**
	 * 标识
	 */
	private final String Prefix;

	System(String prefix) {
		this.Prefix = prefix;
	}
}
