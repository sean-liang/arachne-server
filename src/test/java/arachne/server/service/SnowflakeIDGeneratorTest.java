package arachne.server.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
@SpringBootTest
public class SnowflakeIDGeneratorTest {

    @Autowired
    private SnowflakeIDGenerator generator;

    @Test
    void testGenerate() {
        val id1 = this.generator.nextId();
        val id2 = this.generator.nextId();
        log.info("SnowFlackID #1: {}, #2: {}", id1, id2);
        assertNotEquals(id1, id2);
    }

}
