package arachne.server.service;

import arachne.server.TestDataUtils;
import arachne.server.domain.TargetStatus;
import arachne.server.repository.TargetRepository;
import arachne.server.repository.TargetTaskRepository;
import arachne.server.repository.WorkerRepository;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TargetTaskServiceTest {

    @Autowired
    private WorkerRepository workerRepo;

    @Autowired
    private TargetRepository targetRepo;

    @Autowired
    private TargetTaskRepository taskRepo;

    @Autowired
    private TargetTaskService taskService;

    @AfterEach
    void cleanup() {
        this.workerRepo.deleteAll();
        this.targetRepo.deleteAll();
        this.taskRepo.deleteAll();
    }

    @Test
    void testOnCreated() {
        val target = TestDataUtils.createTarget("test-target").setId("target1");
        this.taskService.onCreated(target);
        assertEquals(1, this.taskRepo.count());
        val task = this.taskRepo.findFirstByTargetIdOrderByStartTimeDesc(target.getId());
        assertNotNull(task);
        assertEquals(target.getCurrentTask(), task);
        assertEquals(0, task.getStartTime());
        assertEquals(0, task.getEndTime());
        assertEquals(1, task.getLogs().size());
        assertTrue(task.getLogs().get(0).getTimestamp() > 0);
        assertEquals("CREATE", task.getLogs().get(0).getAction());
        assertNull(task.getLogs().get(0).getDetail());
    }

    @Test
    void testOnRemoved() {
        val target = TestDataUtils.createTarget("test-target").setId("target1");
        this.taskService.onCreated(target);
        assertEquals(1, this.taskRepo.count());

        this.taskService.onDestroyed(target);
        assertEquals(0, this.taskRepo.count());
    }

    @Test
    void testOnStatusChanged() {
        val target = TestDataUtils.createTarget("test-target").setId("target1");
        this.taskService.onCreated(target);

        target.setStatus(TargetStatus.RUNNING);
        this.taskService.onStatusChanged(target, TargetStatus.SCHEDULED, null);
        var task = this.taskRepo.findFirstByTargetIdOrderByStartTimeDesc(target.getId());
        assertEquals(target.getCurrentTask(), task);
        assertTrue(task.getStartTime() > 0);
        assertEquals("RUNNING", task.getLogs().get(1).getAction());
        assertNull(task.getLogs().get(1).getDetail());

        target.setStatus(TargetStatus.DONE);
        this.taskService.onStatusChanged(target, TargetStatus.RUNNING, null);
        task = this.taskRepo.findFirstByTargetIdOrderByStartTimeDesc(target.getId());
        assertEquals(target.getCurrentTask(), task);
        assertTrue(task.getStartTime() > 0);
        assertTrue(task.getEndTime() > 0);
        assertEquals("DONE", task.getLogs().get(2).getAction());
        assertNull(task.getLogs().get(2).getDetail());
    }

}
