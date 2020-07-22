package arachne.server.job;

import arachne.server.TestDataUtils;
import arachne.server.domain.Job;
import arachne.server.domain.Target;
import arachne.server.domain.TargetStatus;
import arachne.server.domain.Worker;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WeightedLoadBalanceJobScheduleStrategyTest {

    @Autowired
    private WeightedLoadBalanceJobScheduleStrategy strategy;

    @MockBean
    private TrackedJobAllocator allocator;

    @Test
    void testTakeJobs() {
        val worker = TestDataUtils.createWorker("worker1").setId("1").setBatchSize(4);
        val target1 = TestDataUtils.createTarget("target1", worker).setId("1").setStatus(TargetStatus.RUNNING).setWeight(100);
        val target2 = TestDataUtils.createTarget("target2", worker).setId("2").setStatus(TargetStatus.RUNNING).setWeight(200);

        when(allocator.allocate(any(Worker.class), any(Target.class), anyInt())).then(arg -> {
            final Target target = arg.getArgument(1, Target.class);
            final int size = arg.getArgument(2, Integer.class);
            final List<Job> jobs = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                jobs.add(TestDataUtils.createJob(worker.getId(), target.getId(), TestDataUtils.createJobAction("http://someurl")));
            }
            return jobs;
        });

        val jobs = this.strategy.takeJobs(worker, Arrays.asList(target1, target2));
        assertEquals(4, jobs.size());
        assertEquals("2", jobs.get(0).getTargetId());
        assertEquals("2", jobs.get(1).getTargetId());
        assertEquals("2", jobs.get(2).getTargetId());
        assertEquals("1", jobs.get(3).getTargetId());
    }

    @Test
    void testTakeJobsDrainFirst() {
        val worker = TestDataUtils.createWorker("worker1").setId("1").setBatchSize(4);
        val target1 = TestDataUtils.createTarget("target1", worker).setId("1").setStatus(TargetStatus.RUNNING).setWeight(100);
        val target2 = spy(TestDataUtils.createTarget("target2", worker).setId("2").setStatus(TargetStatus.RUNNING).setWeight(200));
        doNothing().when(target2).stop();

        when(allocator.allocate(any(Worker.class), any(Target.class), anyInt())).then(arg -> {
            final Target target = arg.getArgument(1, Target.class);
            int size = arg.getArgument(2, Integer.class);
            if ("2".equals(target.getId())) {
                size = 1;
            }
            final List<Job> jobs = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                jobs.add(TestDataUtils.createJob(worker.getId(), target.getId(), TestDataUtils.createJobAction("http://someurl")));
            }
            return jobs;
        });

        val jobs = this.strategy.takeJobs(worker, Arrays.asList(target1, target2));
        verify(target2, times(1)).stop();
        assertEquals(4, jobs.size());
        assertEquals("2", jobs.get(0).getTargetId());
        assertEquals("1", jobs.get(1).getTargetId());
        assertEquals("1", jobs.get(2).getTargetId());
        assertEquals("1", jobs.get(3).getTargetId());
    }

    @Test
    void testTakeJobsDrainAll() {
        val worker = TestDataUtils.createWorker("worker1").setId("1").setBatchSize(4);
        val target1 = spy(TestDataUtils.createTarget("target1", worker).setId("1").setStatus(TargetStatus.RUNNING).setWeight(100));
        val target2 = spy(TestDataUtils.createTarget("target2", worker).setId("2").setStatus(TargetStatus.RUNNING).setWeight(200));
        doNothing().when(target1).stop();
        doNothing().when(target2).stop();

        when(allocator.allocate(any(Worker.class), any(Target.class), anyInt())).then(arg -> {
            final Target target = arg.getArgument(1, Target.class);
            return Arrays.asList(TestDataUtils.createJob(worker.getId(), target.getId(), TestDataUtils.createJobAction("http://someurl")));
        });

        val jobs = this.strategy.takeJobs(worker, Arrays.asList(target1, target2));
        verify(target1, times(1)).stop();
        verify(target2, times(1)).stop();
        assertEquals(2, jobs.size());
        assertEquals("2", jobs.get(0).getTargetId());
        assertEquals("1", jobs.get(1).getTargetId());
    }

}
