package arachne.server.controller.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class JobStatsUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<TargetJobStatsUpdate> targets;

    private final List<WorkerJobStatsUpdate> workers;

}
