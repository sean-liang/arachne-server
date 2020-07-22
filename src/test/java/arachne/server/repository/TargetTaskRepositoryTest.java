package arachne.server.repository;

import arachne.server.domain.TargetTask;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
public class TargetTaskRepositoryTest {

    @Autowired
    private TargetTaskRepository repo;

    @AfterEach
    void cleanup() {
        this.repo.deleteAll();
    }

    @Test
    void testFindFirstByTargetIdOrderByStartTimeDesc() {
        val task1 = TargetTask.builder().targetId("target1").startTime(100).endTime(200).build();
        val task2 = TargetTask.builder().targetId("target2").startTime(200).endTime(300).build();
        val task3 = TargetTask.builder().targetId("target1").startTime(50).endTime(150).build();

        this.repo.saveAll(Arrays.asList(task1, task2, task3));

        val found = this.repo.findFirstByTargetIdOrderByStartTimeDesc("target1");
        assertNotNull(found);
        assertEquals(100, found.getStartTime());
    }

    @Test
    void testDeleteByTargetId() {
        val task1 = TargetTask.builder().targetId("target1").startTime(100).endTime(200).build();
        val task2 = TargetTask.builder().targetId("target2").startTime(100).endTime(200).build();

        this.repo.saveAll(Arrays.asList(task1, task2));

        assertEquals(2, this.repo.count());

        this.repo.deleteByTargetId("target1");

        val remaining = this.repo.findAll();
        assertEquals(1, remaining.size());
        assertEquals("target2", remaining.get(0).getTargetId());
    }

}
