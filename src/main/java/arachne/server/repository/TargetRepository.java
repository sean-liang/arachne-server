package arachne.server.repository;

import arachne.server.domain.Target;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TargetRepository extends MongoRepository<Target, String> {

}
