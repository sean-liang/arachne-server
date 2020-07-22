package arachne.server.domain.target.store;

import arachne.server.domain.Target;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Transient;

@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractTargetStore implements TargetStore {

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

}
