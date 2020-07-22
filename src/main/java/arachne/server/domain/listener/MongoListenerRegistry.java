package arachne.server.domain.listener;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoListenerRegistry {

    @Bean
    public TargetEventListener targetEventListener() {
        return new TargetEventListener();
    }

}
