package com.hfhk.common.check.service.modules.serial;

import com.hfhk.common.check.service.domain.mongo.Mongo;
import com.hfhk.common.check.service.domain.mongo.SerialMongoV1;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class SerialService {
	private final MongoTemplate mongoTemplate;

	public SerialService(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public long next(String key) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(key));
		query.fields().include("value");

		Update update = new Update();
		update.inc("value");

		return Optional.ofNullable(
			mongoTemplate.findAndModify(
				query,
				update,
				FindAndModifyOptions.options().upsert(true),
				SerialMongoV1.class,
				Mongo.Collection.SERIAL
			))
			.map(SerialMongoV1::getValue)
			.orElse(0L);
	}

	public void create(String key, long initValue, String name) {
		SerialMongoV1.builder()
			.id(key)
			.name(name)
			.value(initValue)
			.build();
		mongoTemplate.insert(SerialMongoV1.class, Mongo.Collection.SERIAL);
	}
}
