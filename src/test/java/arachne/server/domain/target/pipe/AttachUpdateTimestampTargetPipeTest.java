package arachne.server.domain.target.pipe;

import arachne.server.MongoInstance;
import arachne.server.TestDataUtils;
import arachne.server.domain.JobFeedbackContentType;
import arachne.server.domain.feedback.JobFeedback;
import arachne.server.domain.target.store.MongoDocumentTargetStore;
import lombok.val;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Import({MongoInstance.class})
public class AttachUpdateTimestampTargetPipeTest {

    @Autowired
    private MongoTemplate mongo;

    private String col = UUID.randomUUID().toString();

    @Test
    void testInitializeAndDestroy() {
        val pipe = this.createPipe();
        val indexOp = mongo.indexOps(this.col);

        pipe.initialize();
        val indexCreated = new AtomicBoolean(false);
        indexOp.getIndexInfo().forEach(info -> {
            if (info.getIndexFields().size() == 1 &&
                    info.getIndexFields().get(0).getKey().equals("updatedAt") &&
                    info.getIndexFields().get(0).getDirection() == Sort.Direction.DESC) {
                indexCreated.set(true);
            }
        });
        assertTrue(indexCreated.get());

        indexCreated.set(false);
        pipe.destroy();
        indexOp.getIndexInfo().forEach(info -> {
            if (info.getIndexFields().size() == 1 &&
                    info.getIndexFields().get(0).getKey().equals("updatedAt")) {
                indexCreated.set(true);
            }
        });
        assertFalse(indexCreated.get());
    }

    @Test
    void testProceed() throws DropFeedbackException {
        val pipe = this.createPipe();
        val feedback = new JobFeedback(JobFeedbackContentType.JSON, 1L, 200, null, "{}".getBytes());
        val context = new HashMap<String, Object>();

        val result = pipe.proceed(Stream.of(feedback.content()), feedback, context);
        assertNotNull(result);
        val list = result.toArray();
        assertEquals(1, list.length);
        assertTrue(list[0] instanceof Document);
        val doc = (Document)list[0];
        assertTrue(doc.containsKey("updatedAt"));
        assertTrue(System.currentTimeMillis() - (long) doc.get("updatedAt") < 1000);
    }

    private AttachUpdateTimestampTargetPipe createPipe() {
        val worker = TestDataUtils.createWorker("w1");
        val target = TestDataUtils.createTarget("t1", worker);
        target.setStore(new MongoDocumentTargetStore(this.col, "bizid"));
        val pipe = new AttachUpdateTimestampTargetPipe("updatedAt", true, false);
        pipe.setTarget(target);
        return pipe;
    }

}
