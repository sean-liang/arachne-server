package arachne.server.domain.target.store;

import arachne.server.mongo.MongoInstance;
import com.mongodb.WriteConcern;
import lombok.val;
import org.bson.Document;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.SessionSynchronization;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

//@Disabled
@DataMongoTest
@Import({MongoInstance.class})
class MongoDocumentTargetStoreTest {

    @Autowired
    private MongoTemplate template;

    @Test
    void testSetup() throws Exception {
        template.setWriteConcern(WriteConcern.ACKNOWLEDGED);
        val col = UUID.randomUUID().toString();
        assertFalse(template.collectionExists(col));
        val store = MongoDocumentTargetStore.builder().collection(col).idField("bizid").build();
        store.initialize();

        assertTrue(template.collectionExists(col));

        val indexBuilt = new AtomicBoolean(false);
        template.indexOps(col).getIndexInfo().forEach(index -> {
            if (index.getIndexFields().get(0).getKey().equals("bizid")) {
                indexBuilt.set(true);
            }
        });
        assertTrue(indexBuilt.get());
    }

    @Test
    void testDestroy() {
        val col = UUID.randomUUID().toString();
        val store = MongoDocumentTargetStore.builder().collection(col).idField("bizid").build();
        store.initialize();
        assertTrue(template.collectionExists(col));

        store.destroy();
        assertFalse(template.collectionExists(col));
    }

    @Test
    void testSave() {
        val col = UUID.randomUUID().toString();
        val store = MongoDocumentTargetStore.builder().collection(col).idField("bizid").build();
        store.initialize();
        assertTrue(template.collectionExists(col));

        store.save(Document.parse("{'bizid': 'id1', 'k1': 'v1'}"));
        var found = template.find(new Query(where("bizid").is("id1")), Document.class, col);
        assertEquals(1, found.size());
        assertEquals("v1", found.get(0).get("k1"));

        store.save(Document.parse("{'bizid': 'id1', 'k1': 'v2'}"));
        found = template.find(new Query(where("bizid").is("id1")), Document.class, col);
        assertEquals(1, found.size());
        assertEquals("v2", found.get(0).get("k1"));

    }

    @Test
    void testCount() {
        val col = UUID.randomUUID().toString();
        val store = MongoDocumentTargetStore.builder().collection(col).idField("bizid").build();
        store.initialize();

        assertEquals(0, store.count());
        store.save(Document.parse("{'bizid': 'id1', 'k1': 'v1'}"));
        assertEquals(1, store.count());
    }

    @Test
    void testEnsureIndexAndRemoveIndex() {
        val col = UUID.randomUUID().toString();
        val store = MongoDocumentTargetStore.builder().collection(col).idField("bizid").build();
        val indexOp = this.template.indexOps(col);

        store.ensureIndex("indexfld", IndexOrder.DESC, true);
        val indexCreated = new AtomicBoolean(false);
        indexOp.getIndexInfo().forEach(info -> {
            if (info.getIndexFields().size() == 1 &&
                    info.getIndexFields().get(0).getKey().equals("indexfld") &&
                    info.getIndexFields().get(0).getDirection() == Sort.Direction.DESC) {
                indexCreated.set(true);
            }
        });
        assertTrue(indexCreated.get());

        indexCreated.set(false);
        store.removeIndex("indexfld");
        indexOp.getIndexInfo().forEach(info -> {
            if (info.getIndexFields().size() == 1 &&
                    info.getIndexFields().get(0).getKey().equals("indexfld")) {
                indexCreated.set(true);
            }
        });
        assertFalse(indexCreated.get());
    }

}
