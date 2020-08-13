package arachne.server.mongo;

import java.util.function.Supplier;

/**
 * Run mongo operations transactional if it's supported
 */
public interface MongoTransactionAwareExecutor {

    public <T> T execute(final Supplier<T> execution);

    public void execute(final Runnable execution);

}
