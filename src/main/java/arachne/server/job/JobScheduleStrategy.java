package arachne.server.job;

import arachne.server.domain.Job;
import arachne.server.domain.Target;
import arachne.server.domain.Worker;

import java.util.List;

public interface JobScheduleStrategy {

    List<Job> takeJobs(Worker worker, List<Target> targets);

}
