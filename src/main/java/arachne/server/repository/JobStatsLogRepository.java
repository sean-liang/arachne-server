package arachne.server.repository;

import arachne.server.domain.JobStatsLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JobStatsLogRepository extends MongoRepository<JobStatsLog, String>, JobStatsLogRepositoryCustom {


}
