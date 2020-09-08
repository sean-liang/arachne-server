package arachne.server.service;

import arachne.server.controller.admin.form.WorkerForm;
import arachne.server.domain.WorkerProtocol;
import arachne.server.domain.WorkerStatus;
import arachne.server.repository.WorkerRepository;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class WorkerServiceTest {

    @Autowired
    private WorkerService workerService;

    @Autowired
    private WorkerRepository repo;

    @AfterEach
    void cleanup() {
        this.repo.deleteAll();
    }

    @Test
    void testCRUD() {
        val createForm = WorkerForm.builder()
                .name("name1")
                .tags(Arrays.asList("JP", "TEST"))
                .protocol(WorkerProtocol.HTTP)
                .managed(true)
                .status(WorkerStatus.DISABLED)
                .batchSize(100)
                .build();
        assertEquals(0, this.repo.count());

        val saved = this.workerService.addWorker(createForm);
        assertEquals(1, this.repo.count());
        assertEquals(createForm.getName(), saved.getName());
        assertEquals(createForm.getTags(), saved.getTags());
        assertEquals(createForm.getProtocol(), saved.getProtocol());
        assertEquals(createForm.isManaged(), saved.isManaged());
        assertEquals(createForm.getStatus(), saved.getStatus());
        assertEquals(createForm.getBatchSize(), saved.getBatchSize());

        val get = this.workerService.getById(saved.getId());
        assertTrue(get.isPresent());
        assertEquals(get.get().getId(), saved.getId());
        assertEquals(get.get().getName(), saved.getName());
        assertEquals(get.get().getTags(), saved.getTags());
        assertEquals(get.get().getProtocol(), saved.getProtocol());
        assertEquals(get.get().isManaged(), saved.isManaged());
        assertEquals(get.get().getStatus(), saved.getStatus());
        assertEquals(get.get().getBatchSize(), saved.getBatchSize());

        val updateForm = WorkerForm.builder()
                .name("name2")
                .tags(Arrays.asList("US", "PROD"))
                .protocol(WorkerProtocol.WebSocket)
                .managed(true)
                .status(WorkerStatus.NORMAL)
                .batchSize(20)
                .build();
        val updated = this.workerService.updateWorker(saved.getId(), updateForm);
        assertEquals(updateForm.getName(), updated.getName());
        assertEquals(updateForm.getTags(), updated.getTags());
        assertEquals(updateForm.getProtocol(), updated.getProtocol());
        assertEquals(updateForm.isManaged(), updated.isManaged());
        assertEquals(updateForm.getStatus(), updated.getStatus());
        assertEquals(updateForm.getBatchSize(), updated.getBatchSize());

        val foundList = this.workerService.getWorkers();
        assertEquals(1, foundList.size());

        this.workerService.removeWorker(updated.getId());
        assertEquals(0, this.repo.count());
    }

    @Test
    void testGetWorkersPageable() {
        val createForm1 = WorkerForm.builder()
                .name("name1")
                .tags(Arrays.asList("JP", "TEST"))
                .protocol(WorkerProtocol.HTTP)
                .managed(true)
                .status(WorkerStatus.DISABLED)
                .batchSize(100)
                .build();
        val createForm2 = WorkerForm.builder()
                .name("name2")
                .tags(Arrays.asList("JP", "TEST"))
                .protocol(WorkerProtocol.HTTP)
                .managed(true)
                .status(WorkerStatus.DISABLED)
                .batchSize(100)
                .build();
        val createForm3 = WorkerForm.builder()
                .name("name3")
                .tags(Arrays.asList("JP", "TEST"))
                .protocol(WorkerProtocol.HTTP)
                .managed(true)
                .status(WorkerStatus.DISABLED)
                .batchSize(100)
                .build();

        this.workerService.addWorker(createForm1);
        this.workerService.addWorker(createForm2);
        this.workerService.addWorker(createForm3);

        val found = this.workerService.getWorkers(PageRequest.of(1, 1));
        assertEquals(3, found.getTotalElements());
        assertEquals(1, found.getNumberOfElements());
        assertEquals(1, found.getNumber());
        assertEquals(1, found.getSize());
        assertEquals(1, found.getContent().size());
    }

    @Test
    void testGetUniqueTags() {
        val createForm1 = WorkerForm.builder()
                .name("name1")
                .tags(Arrays.asList("JP", "TEST"))
                .protocol(WorkerProtocol.HTTP)
                .managed(true)
                .status(WorkerStatus.DISABLED)
                .batchSize(100)
                .build();
        val createForm2 = WorkerForm.builder()
                .name("name2")
                .tags(Arrays.asList("JP", "TEST"))
                .protocol(WorkerProtocol.HTTP)
                .managed(true)
                .status(WorkerStatus.DISABLED)
                .batchSize(100)
                .build();
        val createForm3 = WorkerForm.builder()
                .name("name3")
                .tags(Arrays.asList("US", "PROD"))
                .protocol(WorkerProtocol.HTTP)
                .managed(true)
                .status(WorkerStatus.DISABLED)
                .batchSize(100)
                .build();

        this.workerService.addWorker(createForm1);
        this.workerService.addWorker(createForm2);
        this.workerService.addWorker(createForm3);

        val tags = this.workerService.getUniqueTags();
        assertEquals(Arrays.asList("JP", "PROD", "TEST", "US"), tags);
    }

}
