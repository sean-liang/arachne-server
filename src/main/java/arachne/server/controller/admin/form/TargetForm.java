package arachne.server.controller.admin.form;

import arachne.server.domain.RetryStrategy;
import arachne.server.domain.Target;
import arachne.server.domain.TargetRepetition;
import arachne.server.domain.target.actionprovider.TargetActionProvider;
import arachne.server.domain.target.pipe.TargetPipe;
import arachne.server.domain.target.store.TargetStore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private String name;

    private int weight;

    @NotNull
    private RetryStrategy retryStrategy;

    private int retries;

    @NotNull
    private TargetRepetition repetition;

    private String repetitionConfig;

    private boolean cancelPrevious;

    @NotNull
    private Set<String> workers;

    @NotNull
    private TargetActionProvider provider;

    @NotNull
    private TargetStore store;

    private Set<TargetPipe> pipes;

    public void applyTo(final Target target) {
        target.setName(this.name);
        target.setWeight(this.weight);
        target.setRetryStrategy(this.retryStrategy);
        target.setRetries(this.retries);
        target.setRepetition(this.repetition);
        target.setRepetitionConfig(this.repetitionConfig);
        target.setCancelPrevious(this.cancelPrevious);
        target.setWorkers(this.workers);
        target.setProvider(this.provider);
        target.setStore(this.store);
        target.setPipes(null == this.pipes ? new HashSet<>() : new HashSet<>(this.pipes));
    }

}
