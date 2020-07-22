package arachne.server.repository;

import arachne.server.domain.User;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository repo;

    @AfterEach
    void cleanup() {
        this.repo.deleteAll();
    }

    @Test
    void testFindOneByUsername() {
        val user1 = User.builder().username("user1").password("123").build();
        val user2 = User.builder().username("user2").password("123").build();

        this.repo.saveAll(Arrays.asList(user1, user2));

        val found = this.repo.findOneByUsername("user1");
        assertNotNull(found);
        assertEquals(user1.getUsername(), found.getUsername());
    }

}
