package arachne.server.mongo;

import arachne.server.domain.User;
import com.mongodb.client.MongoCollection;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Component
public final class MongoInstance {

    private static MongoTemplate mongoTemplate;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public MongoInstance(@Autowired MongoTemplate template) {
        mongoTemplate = template;
    }

    public static MongoTemplate template() {
        return mongoTemplate;
    }

    public static MongoCollection<Document> collectionByName(@NonNull final String name) {
        return template().getCollection(name);
    }

    public static MongoCollection<Document> collection(final Class<?> cls) {
        return collectionByName(template().getCollectionName(cls));
    }

    @PostConstruct
    public void init() {
        final User found = mongoTemplate.findOne(new Query(where("username").is("arachne")), User.class);
        if (null == found) {
            mongoTemplate.insert(new User(null, "arachne", encoder.encode("12345678")));
            log.info("Create admin user, username: arachne, password: 12345678");
        }
    }

}
