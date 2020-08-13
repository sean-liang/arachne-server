package arachne.server.domain;

import arachne.server.exceptions.ServerFailureException;
import arachne.server.mongo.MongoInstance;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.Serializable;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public interface DomainEntity extends Serializable {

    String NO_SUCH_ENTITY = "NO_SUCH_ENTITY:";

    private static Update addUpdateAttributes(final Update update) {
        return update;
    }

    String getId();

    default Target findAndModifyById(final Update update, final FindAndModifyOptions options) {
        final Target updated = MongoInstance.template().findAndModify(
                new Query(where("id").is(this.getId())),
                addUpdateAttributes(update),
                options, Target.class);
        if (null == updated) {
            throw new ServerFailureException(NO_SUCH_ENTITY + this.getId());
        }
        return updated;
    }

    default Target findAndModifyById(final Update update) {
        return this.findAndModifyById(addUpdateAttributes(update), FindAndModifyOptions.none());
    }

    default UpdateResult updateFirstById(final Update update) {
        final UpdateResult result = MongoInstance.template().updateFirst(new Query(where("id").is(this.getId())),
                addUpdateAttributes(update), Target.class);
        if (result.getModifiedCount() < 1) {
            throw new ServerFailureException(NO_SUCH_ENTITY + this.getId());
        }
        return result;
    }

}
