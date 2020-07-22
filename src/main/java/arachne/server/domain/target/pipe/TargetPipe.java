package arachne.server.domain.target.pipe;

import arachne.server.domain.Target;
import arachne.server.domain.feedback.JobFeedback;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Stream;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AttachUpdateTimestampTargetPipe.class, name = "ATTACH_UPDATE_TIMESTAMP_PIPE")
})
public interface TargetPipe extends Serializable {

    void initialize();

    void destroy();

    Target getTarget();

    void setTarget(Target target);

    Stream<Object> proceed(Stream<Object> stream,
                           JobFeedback feedback,
                           Map<String, Object> context) throws Throwable;

}
