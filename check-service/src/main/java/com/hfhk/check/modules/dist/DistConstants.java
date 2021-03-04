package com.hfhk.check.modules.dist;

import com.hfhk.check.mongo.DistCheckMongo;
import com.hfhk.check.mongo.DistProblemMongo;
import org.springframework.data.domain.Sort;

public class DistConstants {
	public static Sort DEFAULT_DIST_CHECK_SORT = Sort.by(
		Sort.Order.asc(DistCheckMongo.FIELD.METADATA.SORT),
		Sort.Order.asc(DistCheckMongo.FIELD.METADATA.CREATED.AT),
		Sort.Order.asc(DistCheckMongo.FIELD.METADATA.LAST_MODIFIED.AT),
		Sort.Order.asc(DistCheckMongo.FIELD._ID)
	);

	public static Sort DEFAULT_DIST_PROBLEM_SORT = Sort.by(
		Sort.Order.asc(DistProblemMongo.FIELD.METADATA.SORT),
		Sort.Order.asc(DistProblemMongo.FIELD.METADATA.CREATED.AT),
		Sort.Order.asc(DistProblemMongo.FIELD.METADATA.LAST_MODIFIED.AT),
		Sort.Order.asc(DistProblemMongo.FIELD._ID)
	);
}
