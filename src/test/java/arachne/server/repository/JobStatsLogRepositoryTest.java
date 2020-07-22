package arachne.server.repository;

import arachne.server.TestDataUtils;
import arachne.server.domain.JobStatsLog;
import arachne.server.domain.stats.TargetJobStats;
import arachne.server.domain.stats.WorkerJobStats;
import arachne.server.service.SystemClockService;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@DataMongoTest
public class JobStatsLogRepositoryTest {

    @Autowired
    private JobStatsLogRepository repo;

    @Autowired
    private MongoTemplate mongo;

    @MockBean
    private SystemClockService clock;

    @Test
    void testPersist() throws InterruptedException {
        val job = TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test"));

        when(clock.currentTimeMinutes(anyLong())).thenReturn(1L);
        when(clock.currentTimeMinutes()).thenReturn(2L);

        val targetStat = new TargetJobStats(clock);
        val workerStat = new WorkerJobStats(clock);
        targetStat.feed(job, "10.10.10.1", true);
        workerStat.feed(job, "10.10.10.2", false);

        this.repo.persistOnDirty("targetId", "t1", targetStat);
        this.repo.persistOnDirty("workerId", "w1", workerStat);

        val targetStats = this.mongo.findOne(new Query(where("targetId").is("t1")), JobStatsLog.class);
        assertNotNull(targetStats);
        assertEquals(3, targetStats.getMeta().size());
        assertEquals(0L, targetStats.getMeta().get("lastRequestAt"));
        assertEquals("10.10.10.1", targetStats.getMeta().get("lastRequestIp"));
        assertEquals("w1", targetStats.getMeta().get("lastRequestWorkerId"));

        val workerStats = this.mongo.findOne(new Query(where("workerId").is("w1")), JobStatsLog.class);
        assertNotNull(workerStats);
        assertEquals(2, workerStats.getMeta().size());
        assertEquals(0L, workerStats.getMeta().get("lastRequestAt"));
        assertEquals("10.10.10.2", workerStats.getMeta().get("lastRequestIp"));
    }

}
