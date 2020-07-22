package arachne.server.controller.admin.dto;

import arachne.server.domain.Target;
import arachne.server.domain.TargetStatus;
import arachne.server.domain.stats.RequestCount;
import arachne.server.domain.stats.TargetJobStats;
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
public class TargetJobStatsUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private TargetStatus status;

    @Builder.Default
    private long cStart= 0;

    @Builder.Default
    private long cEnd = 0;

    @Builder.Default
    private long nextRun = 0;

    @Builder.Default
    private long reqTs = 0;

    private String reqIp;

    private String reqWid;

    @Builder.Default
    private RequestCount r1 = new RequestCount();

    @Builder.Default
    private RequestCount r2 = new RequestCount();

    @Builder.Default
    private RequestCount r3 = new RequestCount();

    public static TargetJobStatsUpdate of(final Target target, final TargetJobStats stats) {
        final TargetJobStatsUpdate update =  TargetJobStatsUpdate
                .builder()
                .id(target.getId())
                .name(target.getName())
                .status(target.getStatus())
                .nextRun(target.getNextRunAt())
                .build();

        if(null != target.getCurrentTask()) {
            update.setCStart(target.getCurrentTask().getStartTime());
            update.setCEnd(target.getCurrentTask().getEndTime());
        }

        if(null != stats) {
            update.setReqTs(stats.getLastRequestAt());
            update.setReqIp(stats.getLastRequestIp());
            update.setReqWid(stats.getLastRequestWorkerId());
            update.setR1(stats.requestCount(0));
            update.setR2(stats.requestCount(10));
            update.setR3(stats.requestCount(60));
        }

        return update;
    }

}
