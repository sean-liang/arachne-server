package arachne.server.util;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntervalTaskRunnerTest {

    private final IntervalTaskRunner runner = new IntervalTaskRunner();

    @Test
    void testRun() throws Exception {
        val counter = new AtomicInteger(0);
        val future = runner.run(10, () -> counter.incrementAndGet());
        Thread.sleep(100);
        future.cancel(true);
        assertTrue(counter.get() > 6 && counter.get() < 11);
    }

}
