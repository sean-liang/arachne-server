package arachne.server.domain.target.actionprovider;

import arachne.server.domain.HttpRequestTemplate;
import arachne.server.domain.Target;
import arachne.server.domain.listener.MongoListenerRegistry;
import arachne.server.domain.target.store.MongoDocumentTargetStore;
import arachne.server.mongo.MongoInstance;
import arachne.server.repository.TargetRepository;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataMongoTest
@Import({MongoListenerRegistry.class, MongoInstance.class})
class TemplateGeneratedTargetActionProviderTest {

    @Autowired
    private TargetRepository repo;

    @Test
    void testUsage() {
        val target = Target.builder().name(UUID.randomUUID().toString())
                .provider(TemplateGeneratedTargetActionProvider.builder()
                        .template(HttpRequestTemplate.builder().url("http://test.com/page?a={{}}&b=10").build())
                        .start(10).end(14).step(2).build())
                .store(MongoDocumentTargetStore.builder().collection("testcol").idField("testfld").build())
                .build();
        target.getProvider().initialize();

        val saved = this.repo.insert(target);
        val found = this.repo.findById(saved.getId()).get();

        final TemplateGeneratedTargetActionProvider provider = found.provider();
        assertEquals("http://test.com/page?a=10&b=10", provider.provide().getUrl());
        assertEquals("http://test.com/page?a=12&b=10", provider.provide().getUrl());
        assertEquals("http://test.com/page?a=14&b=10", provider.provide().getUrl());
        assertNull(provider.provide());

        provider.reset();
        assertEquals("http://test.com/page?a=10&b=10", provider.provide().getUrl());
        assertEquals("http://test.com/page?a=12&b=10", provider.provide().getUrl());
        assertEquals("http://test.com/page?a=14&b=10", provider.provide().getUrl());
        assertNull(provider.provide());
    }

}
