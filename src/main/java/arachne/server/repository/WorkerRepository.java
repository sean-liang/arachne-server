package arachne.server.repository;

import arachne.server.domain.Worker;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkerRepository extends MongoRepository<Worker, String> {

}
