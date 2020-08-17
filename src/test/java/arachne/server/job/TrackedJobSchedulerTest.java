package arachne.server.job;

import arachne.server.TestDataUtils;
import arachne.server.domain.JobFeedbackContentType;
import arachne.server.domain.RetryStrategy;
import arachne.server.domain.TargetStatus;
import arachne.server.service.JobStatsService;
import arachne.server.service.TargetService;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TrackedJobSchedulerTest {

    @Autowired
    private TrackedJobScheduler scheduler;

    @MockBean
    private JobScheduleStrategy strategy;

    @MockBean
    private TargetService targetService;

    @MockBean
    private JobTracker jobTracker;

    @MockBean
    private JobStatsService jobStats;

    @Test
    void testTake() {
        val worker1 = TestDataUtils.createWorker("worker1").setId("w1");
        val worker2 = TestDataUtils.createWorker("worker2").setId("w2");
        val target1 = TestDataUtils.createTarget("target1", worker1).setId("1").setStatus(TargetStatus.DISABLED).setWeight(100);
        val target2 = TestDataUtils.createTarget("target2", worker1).setId("2").setStatus(TargetStatus.RUNNING).setWeight(200);
        val target3 = TestDataUtils.createTarget("target2", worker2).setId("3").setStatus(TargetStatus.RUNNING).setWeight(200);
        val job1 = TestDataUtils.createJob(worker1.getId(), target1.getId(), TestDataUtils.createJobAction("http://url1"));
        val job2 = TestDataUtils.createJob(worker1.getId(), target1.getId(), TestDataUtils.createJobAction("http://url2"));

        when(this.targetService.getTargetStream()).thenReturn(Stream.of(target1, target2, target3));
        when(this.strategy.takeJobs(eq(worker1), any())).then(arg -> {
            val targets = arg.getArgument(1, List.class);
            assertEquals(1, targets.size());
            assertEquals(target2, targets.get(0));
            return Arrays.asList(job1, job2);
        });

        val jobs = this.scheduler.take(worker1);
        assertEquals(2, jobs.size());
    }

    @Test
    void testSuccessFeedback() {
        val worker = TestDataUtils.createWorker("worker1").setId("w1");
        val target = spy(TestDataUtils.createTarget("target1", worker).setId("1").setStatus(TargetStatus.RUNNING).setWeight(100));
        val job = spy(TestDataUtils.createJob(worker.getId(), target.getId(), TestDataUtils.createJobAction("http://url1")).setId(1L));

        when(this.jobTracker.get(eq(job.getId()))).thenReturn(Optional.of(job));
        when(this.jobTracker.remove(eq(1L))).thenReturn(job);
        when(this.targetService.getById(eq(target.getId()))).thenReturn(Optional.of(target));
        doNothing().when(target).feed(any());

        val feedback = TestDataUtils.createJobFeedback(JobFeedbackContentType.JSON, 1L, 200);
        val list = TestDataUtils.createJobFeedbackList("w1", feedback);
        this.scheduler.feed(worker, list);
        verify(target, times(1)).feed(eq(feedback));
    }

    @Test
    void testFailFeedbackAndTargetDisabled() {
        val worker = TestDataUtils.createWorker("worker1").setId("w1");
        val target = spy(TestDataUtils.createTarget("target1", worker).setId("1").setStatus(TargetStatus.DISABLED).setWeight(100));
        val job = spy(TestDataUtils.createJob(worker.getId(), target.getId(), TestDataUtils.createJobAction("http://url1")).setId(1L));

        when(this.jobTracker.get(eq(job.getId()))).thenReturn(Optional.of(job));
        when(this.jobTracker.remove(eq(1L))).thenReturn(job);
        when(this.targetService.getById(eq(target.getId()))).thenReturn(Optional.of(target));
        doNothing().when(target).feed(any());

        val feedback = TestDataUtils.createJobFeedback(JobFeedbackContentType.JSON, 1L, 404);
        val list = TestDataUtils.createJobFeedbackList("w1", feedback);
        this.scheduler.feed(worker, list);
        verify(target, never()).feed(any());
    }

    @Test
    void testFailFeedbackAndNeedNoRetry() {
        val worker = TestDataUtils.createWorker("worker1").setId("w1");
        val target = spy(TestDataUtils
                .createTarget("target1", worker)
                .setId("1")
                .setStatus(TargetStatus.RUNNING)
                .setWeight(100)
                .setRetryStrategy(RetryStrategy.CLUSTER)
                .setRetries(1));
        val job = TestDataUtils.createJob(worker.getId(), target.getId(), TestDataUtils.createJobAction("http://url1")).setId(1L);

        when(this.jobTracker.get(eq(job.getId()))).thenReturn(Optional.of(job));
        when(this.jobTracker.remove(eq(1L))).thenReturn(job);
        when(this.targetService.getById(eq(target.getId()))).thenReturn(Optional.of(target));
        doNothing().when(target).feed(any());

        val feedback = TestDataUtils.createJobFeedback(JobFeedbackContentType.JSON, 1L, 404);
        val list = TestDataUtils.createJobFeedbackList("w1", feedback);
        this.scheduler.feed(worker, list);
        verify(target, times(1)).feed(any());
    }

    @Test
    void testFailFeedbackAndNeedRetry() {
        val worker = TestDataUtils.createWorker("worker1").setId("w1");
        val target = spy(TestDataUtils
                .createTarget("target1", worker)
                .setId("1")
                .setStatus(TargetStatus.RUNNING)
                .setWeight(100)
                .setRetryStrategy(RetryStrategy.CLUSTER)
                .setRetries(3));
        val job = TestDataUtils.createJob(worker.getId(), target.getId(), TestDataUtils.createJobAction("http://url1")).setId(1L);

        when(this.jobTracker.get(eq(job.getId()))).thenReturn(Optional.of(job));
        when(this.jobTracker.remove(eq(1L))).thenReturn(job);
        when(this.targetService.getById(eq(target.getId()))).thenReturn(Optional.of(target));
        doNothing().when(target).feed(any());

        val feedback = TestDataUtils.createJobFeedback(JobFeedbackContentType.JSON, 1L, 404);
        val list = TestDataUtils.createJobFeedbackList("w1", feedback);
        this.scheduler.feed(worker, list);
        verify(target, never()).feed(any());
    }

    @Test
    void testFailFeedbackAndNotClusterRetry() {
        val worker = TestDataUtils.createWorker("worker1").setId("w1");
        val target = spy(TestDataUtils
                .createTarget("target1", worker)
                .setId("1")
                .setStatus(TargetStatus.RUNNING)
                .setWeight(100)
                .setRetryStrategy(RetryStrategy.LOCAL)
                .setRetries(1));
        val job = TestDataUtils.createJob(worker.getId(), target.getId(), TestDataUtils.createJobAction("http://url1")).setId(1L);

        when(this.jobTracker.get(eq(job.getId()))).thenReturn(Optional.of(job));
        when(this.jobTracker.remove(eq(1L))).thenReturn(job);
        when(this.targetService.getById(eq(target.getId()))).thenReturn(Optional.of(target));
        doNothing().when(target).feed(any());

        val feedback = TestDataUtils.createJobFeedback(JobFeedbackContentType.JSON, 1L, 404);
        val list = TestDataUtils.createJobFeedbackList("w1", feedback);
        this.scheduler.feed(worker, list);
        verify(target, times(1)).feed(any());
    }

}
