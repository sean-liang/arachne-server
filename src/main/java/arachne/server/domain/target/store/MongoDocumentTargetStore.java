package arachne.server.domain.target.store;

import arachne.server.mongo.MongoInstance;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mongodb.client.MongoCollection;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@JsonTypeName("MONGO_DOCUMENT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MongoDocumentTargetStore extends AbstractTargetStore implements IndexedDocumentTargetStore {

    private static final long serialVersionUID = 1L;

    private String collection;

    private String idField;

    @Override
    public synchronized void initialize() {
        final MongoTemplate template = MongoInstance.template();
        if (template.collectionExists(this.collection)) {
            template.dropCollection(this.collection);
        }
        template.createCollection(this.collection);
        this.ensureIndex(this.idField, IndexOrder.ASC, true);
    }

    @Override
    public void destroy() {
        MongoInstance.template().dropCollection(this.collection);
    }

    @Override
    public void save(final Object data) {
        if (!(data instanceof Document)) {
            log.debug("Drop unsupported data type: {}", data.getClass().getCanonicalName());
            return;
        }
        final Document doc = (Document) data;
        MongoInstance.template().findAndReplace(new Query(where(idField).is(doc.get(idField).toString())), doc,
                FindAndReplaceOptions.options().upsert(), Document.class, this.collection);
    }

    @Override
    public long count() {
        return this.collection().estimatedDocumentCount();
    }

    private MongoCollection<Document> collection() {
        return MongoInstance.collectionByName(this.collection);
    }

    @Override
    public void ensureIndex(String name, IndexOrder order, final boolean unique) {
        final IndexOperations indexOp = MongoInstance.template().indexOps(this.collection);
        Index index = new Index().on(name, order == IndexOrder.ASC ? Sort.Direction.ASC : Sort.Direction.DESC);
        if (unique) {
            index = index.unique();
        }
        indexOp.ensureIndex(index);
    }

    @Override
    public void removeIndex(String name) {
        final IndexOperations indexOp = MongoInstance.template().indexOps(this.collection);
        final List<IndexInfo> info = indexOp.getIndexInfo();
        info.forEach(i -> {
            if (i.getIndexFields().size() == 1 && i.getIndexFields().get(0).getKey().equals(name)) {
                indexOp.dropIndex(i.getName());
            }
        });
    }
}
