package arachne.server.job;

import arachne.server.domain.Worker;
import arachne.server.domain.feedback.JobFeedbackMessageList;

public interface FeedbackDispatcher {

    void feed(Worker worker, JobFeedbackMessageList feedback);

}
