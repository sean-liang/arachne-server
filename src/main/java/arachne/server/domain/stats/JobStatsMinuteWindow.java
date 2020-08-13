package arachne.server.domain.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatsMinuteWindow implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private long minute = 0L;

    @Builder.Default
    private int successCount = 0;

    @Builder.Default
    private int failCount = 0;

    public JobStatsMinuteWindow(final long minute) {
        this.minute = minute;
    }

    public int increaseCount(final boolean isSuccess) {
        return isSuccess ? this.successCount++ : this.failCount++;
    }

}
