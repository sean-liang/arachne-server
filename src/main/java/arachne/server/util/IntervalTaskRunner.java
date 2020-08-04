package arachne.server.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Run task at a fixed interval
 */
@Slf4j
public class IntervalTaskRunner {

    /**
     * Run task at a fixed interval
     *
     * @param millis interval in milliseconds
     * @param runnable task
     * @return Future representing pending completion of the task
     */
    public static Future<?> run(final int millis, final Runnable runnable) {
        return Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                if (Thread.interrupted()) {
                    break;
                }
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException ex) {
                    log.warn("Interrupted", ex);
                    Thread.currentThread().interrupt();
                }
                runnable.run();
            }
        });
    }

}
