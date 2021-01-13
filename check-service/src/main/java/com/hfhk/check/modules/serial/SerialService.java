package com.hfhk.check.modules.serial;

import com.hfhk.check.mongo.Mongo;
import com.hfhk.check.mongo.SerialMongo;
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
		query.addCriteria(Criteria.where(SerialMongo.FIELD._ID).is(key));
		query.fields().include(SerialMongo.FIELD.VALUE);

		Update update = new Update();
		update.inc(SerialMongo.FIELD.VALUE);

		return Optional.ofNullable(
			mongoTemplate.findAndModify(
				query,
				update,
				FindAndModifyOptions.options().upsert(true),
				SerialMongo.class,
				Mongo.Collection.SERIAL
			))
			.map(SerialMongo::getValue)
			.orElse(0L);
	}

	public void create(String key, long initValue, String name) {
		SerialMongo.builder()
			.id(key)
			.name(name)
			.value(initValue)
			.build();
		mongoTemplate.insert(SerialMongo.class, Mongo.Collection.SERIAL);
	}
}
