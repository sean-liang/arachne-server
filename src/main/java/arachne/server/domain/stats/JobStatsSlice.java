package arachne.server.domain.stats;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class JobStatsSlice {

    @Getter
    private final Stream<JobStatsMinuteWindow> windows;

    @Getter
    private final Map<String, Serializable> meta = new HashMap<>();

    public JobStatsSlice(Stream<JobStatsMinuteWindow> windows) {
        this.windows = windows;
    }
}
