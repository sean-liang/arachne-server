package arachne.server.mongo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

@Slf4j
@Configuration
public class MongoTransactionConfiguration {

    @Bean
    @ConditionalOnProperty(
            value = "arachne.transaction.enabled",
            havingValue = "true",
            matchIfMissing = false)
    MongoTransactionManager transactionManager(final MongoDatabaseFactory dbFactory) {
        log.info("Enable mongodb transaction support");
        dbFactory.isTransactionActive();
        return new MongoTransactionManager(dbFactory);
    }

    @Bean
    @ConditionalOnProperty(
            value = "arachne.transaction.enabled",
            havingValue = "true",
            matchIfMissing = false)
    MongoTransactionAwareExecutor transactionExecutor(final MongoTransactionManager manager) {
        return new MongoTransactionExecutor(manager);
    }

    @Bean
    @ConditionalOnProperty(
            value = "arachne.transaction.enabled",
            havingValue = "false",
            matchIfMissing = true)
    MongoTransactionAwareExecutor noTransactionExecutor() {
        return new MongoNoTransactionExecutor();
    }
}
