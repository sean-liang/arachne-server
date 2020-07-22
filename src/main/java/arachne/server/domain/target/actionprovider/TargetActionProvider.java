package arachne.server.domain.target.actionprovider;

import arachne.server.domain.JobAction;
import arachne.server.domain.Target;
import arachne.server.domain.feedback.JobFeedback;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TemplateGeneratedTargetActionProvider.class, name = "TEMPLATE_GENERATED")
})
public interface TargetActionProvider extends Serializable {

    void initialize();

    void destroy();

    Target getTarget();

    void setTarget(Target target);

    JobAction provide();

    List<JobAction> provide(int size);

    void reset();

    void persistOnDirty();

    void feed(Object data, JobFeedback feedback, Map<String, Object> context);

}
