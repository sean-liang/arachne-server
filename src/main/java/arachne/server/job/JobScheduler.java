package arachne.server.job;

import arachne.server.domain.Job;
import arachne.server.domain.Worker;
import arachne.server.domain.feedback.JobFeedbackMessageList;

import java.util.List;

public interface JobScheduler {

    List<Job> take(Worker worker);

    void feed(Worker worker, JobFeedbackMessageList feedback);

}
