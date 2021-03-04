package com.hfhk.check.mongo;

public class Mongo {

	public static class Collection {
		public static final String PREFIX = "check_";
		public static final String CHECK = PREFIX.concat("checks");
		public static final String SERIAL = PREFIX.concat("serials");
		public static final String PROBLEM = PREFIX.concat("problems");
		public static final String DIST = PREFIX.concat("dist");
		public static final String DIST_CHECK = PREFIX.concat("dist_checks");
		public static final String DIST_PROBLEM = PREFIX.concat("dist_problems");

		public static String collection(String collection) {
			return PREFIX.concat(collection);
		}
	}
}
