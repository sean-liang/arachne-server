package arachne.server.repository;

import arachne.server.domain.TargetTask;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TargetTaskRepository extends MongoRepository<TargetTask, String> {

    public TargetTask findFirstByTargetIdOrderByStartTimeDesc(final String targetId);

    public long deleteByTargetId(final String targetId);

}
