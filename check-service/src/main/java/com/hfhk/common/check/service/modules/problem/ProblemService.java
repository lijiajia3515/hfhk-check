package com.hfhk.common.check.service.modules.problem;

import com.hfhk.common.check.problem.Problem;
import com.hfhk.common.check.service.modules.check.CheckService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import static com.hfhk.common.check.service.modules.problem.ProblemConstants.MONGO_COLLECTION_PROBLEM;

@Service
public class ProblemService {
	private final CheckService checkService;
	private final MongoTemplate mongoTemplate;

	public ProblemService(CheckService checkService, MongoTemplate mongoTemplate) {
		this.checkService = checkService;
		this.mongoTemplate = mongoTemplate;
	}

	public Problem save(ProblemSaveRequest request) {
		ProblemMongoV1 problem = ProblemMongoV1.builder()
			.check(request.getCheck())
			.title(request.getTitle())
			.description(request.getDescription())
			.provision(request.getProvision())
			.measures(request.getMeasures())
			.score(request.getScore())
			.build();
		problem = mongoTemplate.insert(problem, MONGO_COLLECTION_PROBLEM);
		checkService.find()
	}

}
