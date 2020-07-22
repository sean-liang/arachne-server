package arachne.server.domain;

import arachne.server.domain.stats.JobStatsMinuteWindow;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.*;

@Document(collection = "sys_job_stats_log")
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class JobStatsLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Indexed(unique = true, sparse = true)
    private String targetId;

    @Indexed(unique = true, sparse = true)
    private String workerId;

    @Builder.Default
    private Map<String, Serializable> meta = new HashMap<>();

    @Builder.Default
    private List<JobStatsMinuteWindow> logs = new ArrayList<>();

    @JsonIgnore
    @Version
    private Long version;
}
