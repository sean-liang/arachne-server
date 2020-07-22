package arachne.server.repository;

import arachne.server.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    public User findOneByUsername(String username);

}
