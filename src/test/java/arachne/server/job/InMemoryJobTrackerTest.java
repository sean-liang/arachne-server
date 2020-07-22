package arachne.server.job;

import arachne.server.TestDataUtils;
import arachne.server.domain.JobStatus;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class InMemoryJobTrackerTest {

    @Autowired
    private InMemoryJobTracker tracker;

    @MockBean
    private JobTrackerRoutine routine;

    @Test
    void testUsage() {
        val worker = TestDataUtils.createWorker("worker1").setId("w1");
        val job = TestDataUtils.createJob(worker.getId(), "t1", TestDataUtils.createJobAction("http://someurl"));
        assertFalse(job.getId() > 0);
        assertFalse(job.isTracked());
        assertNotEquals(JobStatus.WORKING, job.getStatus());

        val tracked = this.tracker.track(job);
        assertTrue(tracked.getId() > 0);
        assertTrue(job.isTracked());
        assertEquals(JobStatus.WORKING, job.getStatus());
        assertEquals(job, tracked);

        val found = this.tracker.get(tracked.getId());
        assertTrue(found.isPresent());
        assertEquals(tracked, found.get());

        assertEquals(0, this.tracker.retryJobStream(worker, "t1", 2).count());
        found.get().setStatus(JobStatus.PENDING);
        assertEquals(1, this.tracker.retryJobStream(worker, "t1", 2).count());
        assertEquals(0, this.tracker.retryJobStream(worker, "t2", 2).count());

        found.get().setStatus(JobStatus.FAIL);
        assertEquals(1, this.tracker.retryJobStream(worker, "t1", 2).count());
        found.get().getFailedWorkers().add(worker.getId());
        assertEquals(0, this.tracker.retryJobStream(worker, "t1", 2).count());

        val removed = this.tracker.remove(tracked.getId());
        assertEquals(tracked, removed);
        assertFalse(this.tracker.get(tracked.getId()).isPresent());
    }

}
