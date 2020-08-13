package arachne.server.service;

import arachne.server.TestDataUtils;
import arachne.server.mongo.MongoInstance;
import arachne.server.repository.TargetRepository;
import arachne.server.repository.TargetTaskRepository;
import arachne.server.repository.WorkerRepository;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({MongoInstance.class})
public class TargetServiceTest {

    @Autowired
    private WorkerRepository workerRepo;

    @Autowired
    private TargetRepository targetRepo;

    @Autowired
    private TargetTaskRepository taskRepo;

    @Autowired
    private TargetService targetService;

    @MockBean
    private JobStatsService jobStatsService;

    @MockBean
    private TargetRoutine routine;

    @AfterEach
    void cleanup() {
        this.workerRepo.deleteAll();
        this.targetRepo.deleteAll();
        this.taskRepo.deleteAll();
    }

    @Test
    void testCRUD() {
        val worker1 = this.workerRepo.save(TestDataUtils.createWorker("test-worker1"));
        val target = this.targetService.addTarget(TestDataUtils.createTargetForm("test-target", worker1));
        assertEquals(1, this.targetRepo.count());
        val task = this.taskRepo.findFirstByTargetIdOrderByStartTimeDesc(target.getId());
        assertNotNull(task);
        assertEquals(target.getId(), task.getTargetId());

        val worker2 = this.workerRepo.save(TestDataUtils.createWorker("test-worker2"));
        this.targetService.updateTarget(target.getId(), TestDataUtils.createTargetForm("test-target2", worker2));
        val found = this.targetService.getById(target.getId()).get();
        assertEquals("test-target2", found.getName());
        assertEquals(found.getWorkers(), Arrays.asList(worker2.getId()));

        this.targetService.removeTarget(target.getId());
        assertEquals(0, this.targetRepo.count());
        assertNull(this.taskRepo.findFirstByTargetIdOrderByStartTimeDesc(target.getId()));
    }

    @Test
    void testGetTargetsPageable() {
        val worker = this.workerRepo.save(TestDataUtils.createWorker("test-worker"));
        val target1 = TestDataUtils.createTargetForm("test-target1", worker);
        val target2 = TestDataUtils.createTargetForm("test-target2", worker);
        val target3 = TestDataUtils.createTargetForm("test-target3", worker);

        this.targetService.addTarget(target1);
        this.targetService.addTarget(target2);
        this.targetService.addTarget(target3);

        val found = this.targetService.getTargets(PageRequest.of(1, 1));
        assertEquals(3, found.getTotalElements());
        assertEquals(1, found.getNumberOfElements());
        assertEquals(1, found.getNumber());
        assertEquals(1, found.getSize());
        assertEquals(1, found.getContent().size());
    }

}
