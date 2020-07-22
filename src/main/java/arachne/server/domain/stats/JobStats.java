package arachne.server.domain.stats;

import arachne.server.domain.Job;
import arachne.server.domain.feedback.JobFeedbackMessageList;

import java.io.Serializable;
import java.util.function.Consumer;

public interface JobStats extends Serializable {

    void persistIfDirty(Consumer<JobStatsSlice> serializer);

    long getLastRequestAt();

    String getLastRequestIp();

    void feed(Job job, String ip, boolean isSuccess);

    RequestCount requestCount(int minutes);

}
