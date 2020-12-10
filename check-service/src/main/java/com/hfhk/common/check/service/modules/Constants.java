package com.hfhk.common.check.service.modules;

public class Constants {
	public static final String DELIMITER = "-";

	public static class Mongo {
		public static class Collection {
			public static final String PREFIX = "check_";

			public static String collection(String collection) {
				return PREFIX.concat(collection);
			}
		}
	}
}
