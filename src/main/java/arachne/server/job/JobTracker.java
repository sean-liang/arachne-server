package arachne.server.job;

import arachne.server.domain.Job;
import arachne.server.domain.Worker;

import java.util.Optional;
import java.util.stream.Stream;

public interface JobTracker {

    Optional<Job> get(long id);

    Job track(Job job);

    Job remove(long id);

    Stream<Job> retryJobStream(Worker worker, String targetId, int size);

}
