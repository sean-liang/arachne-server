package arachne.server.domain.target.actionprovider;

import arachne.server.domain.JobAction;
import arachne.server.domain.Target;
import arachne.server.domain.feedback.JobFeedback;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractTargetActionProvider implements TargetActionProvider {

    @JsonIgnore
    @Transient
    private transient Target target;

    @Override
    public Target getTarget() {
        return this.target;
    }

    @Override
    public void setTarget(@NonNull final Target target) {
        this.target = target;
    }

    @Override
    public synchronized List<JobAction> provide(final int size) {
        final List<JobAction> actions = new ArrayList<>();
        int count = 0;
        while (count < size) {
            final JobAction action = this.provide();
            if (null == action) {
                break;
            }
            actions.add(action);
            count++;
        }
        return actions;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void persistOnDirty() {

    }

    @Override
    public void feed(final Object data, final JobFeedback feedback, final Map<String, Object> context) {

    }

}
