package arachne.server.domain.target.pipe;

import arachne.server.domain.Target;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Transient;

@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractTargetPipe implements TargetPipe {

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
    public void initialize() {}

    @Override
    public void destroy() {}
}
