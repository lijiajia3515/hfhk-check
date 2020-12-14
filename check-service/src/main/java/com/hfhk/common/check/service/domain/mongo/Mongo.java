package com.hfhk.common.check.service.domain.mongo;

public class Mongo {

	public static class Collection {
		public static final String PREFIX = "check_";
		public static final String CHECK = PREFIX.concat("checks");
		public static final String SERIAL = PREFIX.concat("serials");
		public static final String PROBLEM = PREFIX.concat("problems");
		public static final String SYSTEM_CHECK = PREFIX.concat("system_checks");

		public static String collection(String collection) {
			return PREFIX.concat(collection);
		}
	}
}
