package arachne.server.domain.stats;

import arachne.server.TestDataUtils;
import arachne.server.TestSystemClockService;
import arachne.server.domain.Job;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AbstractJobStatsTest {

    @Test
    void testFeed() {
        val stats = new TestJobStats();

        assertFalse(stats.dirty);

        stats.clock().setMinute(1);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test1")), "10.10.10.1", true);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test2")), "10.10.10.2", false);
        assertTrue(stats.dirty);
        assertEquals(60000L, stats.getLastRequestAt());
        assertEquals("10.10.10.2", stats.getLastRequestIp());
        assertEquals(1, stats.windows.size());
        assertEquals(1L, stats.windows.getLast().getMinute());
        assertEquals(1, stats.windows.getLast().getSuccessCount());
        assertEquals(1, stats.windows.getLast().getFailCount());

        stats.clock().setMinute(2);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test3")), "10.10.10.1", true);
        assertEquals(120000L, stats.getLastRequestAt());
        assertEquals("10.10.10.1", stats.getLastRequestIp());
        assertEquals(2, stats.windows.size());
        assertEquals(2L, stats.windows.getLast().getMinute());
        assertEquals(1, stats.windows.getLast().getSuccessCount());
        assertEquals(0, stats.windows.getLast().getFailCount());
    }

    @Test
    void testRequestCount() {
        val stats = new TestJobStats();
        stats.clock().setMinute(1);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test1")), "10.10.10.1", true);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test2")), "10.10.10.2", false);
        stats.clock().setMinute(2);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test3")), "10.10.10.1", true);
        stats.clock().setMinute(3);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test4")), "10.10.10.2", false);

        assertTrue(stats.onFeedCalled);

        val count0 = stats.requestCount(0);
        assertEquals(0, count0.getSuccessCount());
        assertEquals(1, count0.getFailCount());

        val count1 = stats.requestCount(1);
        assertEquals(1, count1.getSuccessCount());
        assertEquals(0, count1.getFailCount());

        val count2 = stats.requestCount(2);
        assertEquals(2, count2.getSuccessCount());
        assertEquals(1, count2.getFailCount());

        stats.clock().setMinute(5);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test5")), "10.10.10.1", true);
        stats.clock().setMinute(6);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test3")), "10.10.10.2", false);
        val count3 = stats.requestCount(3);
        assertEquals(1, count3.getSuccessCount());
        assertEquals(1, count3.getFailCount());

        stats.clock().setMinute(8);
        val countn = stats.requestCount(0);
        assertEquals(0, countn.getSuccessCount());
        assertEquals(0, countn.getFailCount());
    }

    @Test
    void testPersistIfDirty() {
        val stats = new TestJobStats();
        stats.clock().setMinute(1);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test1")), "10.10.10.1", true);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test2")), "10.10.10.2", false);
        stats.clock().setMinute(61);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test3")), "10.10.10.1", true);
        stats.clock().setMinute(62);
        stats.feed(TestDataUtils.createJob("w1", "t1", TestDataUtils.createJobAction("http://test4")), "10.10.10.2", false);

        stats.persistIfDirty(slice -> {
            assertEquals(62 * 60000L, slice.getMeta().get("lastRequestAt"));
            assertEquals("10.10.10.2", slice.getMeta().get("lastRequestIp"));
            val windows = slice.getWindows().collect(Collectors.toList());
            assertEquals(2, windows.size());
            assertEquals(1L, windows.get(0).getMinute());
            assertEquals(1, windows.get(0).getSuccessCount());
            assertEquals(1, windows.get(0).getFailCount());
            assertEquals(61L, windows.get(1).getMinute());
            assertEquals(1, windows.get(1).getSuccessCount());
            assertEquals(0, windows.get(1).getFailCount());
        });
        assertTrue(stats.dumpMetaCalled);
        assertEquals(62, stats.lastPersistAt);
        assertFalse(stats.dirty);
        assertEquals(2, stats.windows.size());
        assertEquals(61, stats.windows.getFirst().getMinute());
    }

    private class TestJobStats extends AbstractJobStats {
        boolean onFeedCalled = false;
        boolean dumpMetaCalled = false;

        public TestJobStats() {
            super(new TestSystemClockService());
        }

        public TestSystemClockService clock() {
            return (TestSystemClockService) super.clock;
        }

        @Override
        protected void onFeed(Job job, String ip, boolean isSuccess) {
            onFeedCalled = true;
        }

        @Override
        protected void dumpMeta(Map<String, Serializable> meta) {
            dumpMetaCalled = true;
        }
    }
}
