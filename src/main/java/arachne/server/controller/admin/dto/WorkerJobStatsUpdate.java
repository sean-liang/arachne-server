package arachne.server.controller.admin.dto;

import arachne.server.domain.Worker;
import arachne.server.domain.WorkerStatus;
import arachne.server.domain.stats.RequestCount;
import arachne.server.domain.stats.WorkerJobStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class WorkerJobStatsUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private WorkerStatus status;

    @Builder.Default
    private long reqTs = 0;

    private String reqIp;

    @Builder.Default
    private RequestCount r1 = new RequestCount();

    @Builder.Default
    private RequestCount r2 = new RequestCount();

    @Builder.Default
    private RequestCount r3 = new RequestCount();

    public static WorkerJobStatsUpdate of(final Worker worker, final WorkerJobStats stats) {
        final WorkerJobStatsUpdate update = WorkerJobStatsUpdate
                .builder()
                .id(worker.getId())
                .name(worker.getName())
                .status(worker.getStatus())
                .build();

        if (null != stats) {
            update.setReqTs(stats.getLastRequestAt());
            update.setReqIp(stats.getLastRequestIp());
            update.setR1(stats.requestCount(0));
            update.setR2(stats.requestCount(10));
            update.setR3(stats.requestCount(60));
        }

        return update;
    }

}
