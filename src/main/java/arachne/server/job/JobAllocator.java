package arachne.server.job;

import arachne.server.domain.Job;
import arachne.server.domain.Target;
import arachne.server.domain.Worker;

import java.util.List;

public interface JobAllocator {

    List<Job> allocate(Worker worker, Target target, int size);

}
