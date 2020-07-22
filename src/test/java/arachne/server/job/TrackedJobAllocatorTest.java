package arachne.server.job;

import arachne.server.TestDataUtils;
import arachne.server.domain.Job;
import arachne.server.domain.TargetStatus;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TrackedJobAllocatorTest {

    @Autowired
    private TrackedJobAllocator allocator;

    @MockBean
    private JobTracker tracker;

    @Test
    void testAllocate() {
        val worker = TestDataUtils.createWorker("worker1").setId("1");
        val target = TestDataUtils.createTarget("target1", worker).setId("1").setStatus(TargetStatus.RUNNING);

        when(this.tracker.track(any())).then(arg -> {
            val job = arg.getArgument(0, Job.class);
            job.setTracked(true);
            return job;
        });
        when(this.tracker.retryJobStream(worker, target.getId(), 4))
                .thenReturn(
                        Stream.of(
                                TestDataUtils.createJob(worker.getId(), target.getId(), TestDataUtils.createJobAction("http://url1")),
                                TestDataUtils.createJob(worker.getId(), target.getId(), TestDataUtils.createJobAction("http://url2"))
                        ));

        val jobs = this.allocator.allocate(worker, target, 4);

        assertEquals(4, jobs.size());
        assertEquals("http://url1/", jobs.get(0).getAction().getUrl());
        assertEquals("http://url2/", jobs.get(1).getAction().getUrl());
        assertEquals("http://test/page?id=0", jobs.get(2).getAction().getUrl());
        assertEquals("http://test/page?id=1", jobs.get(3).getAction().getUrl());
    }

}
