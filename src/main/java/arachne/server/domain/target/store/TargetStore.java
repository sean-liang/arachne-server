package arachne.server.domain.target.store;

import arachne.server.domain.Target;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MongoDocumentTargetStore.class, name = "MONGO_DOCUMENT")
})
public interface TargetStore extends Serializable {

    void initialize();

    void destroy();

    Target getTarget();

    void setTarget(Target target);

    void save(Object data);

    long count();

}
