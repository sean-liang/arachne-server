package arachne.server.domain;

import arachne.server.job.JobTracker;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Job implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;

    private String targetId;

    private String workerId;

    private JobAction action;

    @Builder.Default
    private JobStatus status = JobStatus.PENDING;

    @Builder.Default
    private int retries = 0;

    @Builder.Default
    private int fails = 0;

    @Builder.Default
    private Set<String> failedWorkers = new HashSet<>();

    private long updatedAt;

    @Builder.Default
    private boolean tracked = false;

    @JsonIgnore
    @Version
    private Long version;

    public synchronized boolean failAndNeedRetry(int retries) {
        this.fails += 1;
        this.updatedAt = System.currentTimeMillis();
        this.status = JobStatus.FAIL;
        this.failedWorkers.add(this.workerId);
        return this.fails < retries;
    }

    public synchronized void updateStatus(final JobStatus status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

}
