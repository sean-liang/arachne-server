package arachne.server.mongo;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class MongoNoTransactionExecutor implements MongoTransactionAwareExecutor {

    @Override
    public <T> T execute(@NonNull final Supplier<T> execution) {
        return execution.get();
    }

    @Override
    public void execute(@NonNull final Runnable execution) {
        execution.run();
    }

}
