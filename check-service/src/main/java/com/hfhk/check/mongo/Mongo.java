package com.hfhk.check.mongo;

public class Mongo {

	public static class Collection {
		public static final String PREFIX = "check_";
		public static final String CHECK = PREFIX.concat("checks");
		public static final String SERIAL = PREFIX.concat("serials");
		public static final String PROBLEM = PREFIX.concat("problems");
		public static final String SYSTEM_DIST = PREFIX.concat("system_dist");
		public static final String SYSTEM_DIST_CHECK = PREFIX.concat("system_dist_checks");
		public static final String SYSTEM_DIST_PROBLEM = PREFIX.concat("system_dist_problems");

		public static String collection(String collection) {
			return PREFIX.concat(collection);
		}
	}
}
