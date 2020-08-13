package arachne.server.domain.stats;

import arachne.server.domain.Job;

import java.io.Serializable;
import java.util.function.Consumer;

public interface JobStats extends Serializable {

    boolean persistIfDirty(Consumer<JobStatsSlice> serializer);

    long getLastRequestAt();

    String getLastRequestIp();

    void feed(Job job, String ip, boolean isSuccess);

    RequestCount requestCount(int minutes);

}
