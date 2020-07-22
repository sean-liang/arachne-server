package arachne.server.domain.listener;

import arachne.server.domain.JobAction;
import arachne.server.domain.Target;
import arachne.server.domain.target.actionprovider.AbstractTargetActionProvider;
import arachne.server.domain.target.store.AbstractTargetStore;
import arachne.server.repository.TargetRepository;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
@Import(MongoListenerRegistry.class)
class TargetEventListenerTest {

    @Autowired
    private TargetRepository repo;

    @Test
    void test() {
        val target = Target
                .builder()
                .name(UUID.randomUUID().toString())
                .provider(new TestTargetActionProvider())
                .store(new TestTargetStore())
                .build();

        val saved = this.repo.save(target);

        val found = this.repo.findById(saved.getId());

        assertTrue(found.isPresent());

        final TestTargetActionProvider provider = found.get().provider();
        assertEquals(saved.getId(), provider.getTarget().getId());
    }

    @NoArgsConstructor
    public static class TestTargetActionProvider extends AbstractTargetActionProvider {
        private static final long serialVersionUID = 1L;

        @Override
        public JobAction provide() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    @NoArgsConstructor
    public static class TestTargetStore extends AbstractTargetStore {
        private static final long serialVersionUID = 1L;

        @Override
        public void initialize() {

        }

        @Override
        public void destroy() {

        }

        @Override
        public void save(Object data) {

        }

        @Override
        public long count() {
            return 0;
        }
    }

}
