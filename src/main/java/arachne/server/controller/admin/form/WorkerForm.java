package arachne.server.controller.admin.form;

import arachne.server.domain.Worker;
import arachne.server.domain.WorkerEngine;
import arachne.server.domain.WorkerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private String name;

    private List<String> tags;

    @NotNull
    private WorkerEngine engine;

    @NotNull
    private WorkerStatus status;

    private int batchSize;

    public void applyTo(final Worker worker) {
        worker.setName(this.name);
        worker.setTags(this.tags);
        worker.setEngine(this.engine);
        worker.setStatus(this.status);
        worker.setBatchSize(this.batchSize);
    }

}
