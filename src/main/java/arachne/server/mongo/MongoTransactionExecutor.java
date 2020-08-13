package arachne.server.mongo;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Slf4j
public class MongoTransactionExecutor implements MongoTransactionAwareExecutor {

    private TransactionTemplate transaction;

    public MongoTransactionExecutor(final @NonNull MongoTransactionManager transactionManager) {
        this.transaction = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T execute(@NonNull final Supplier<T> execution) {
        return this.transaction.execute(new TransactionCallback<T>() {
            @Override
            public T doInTransaction(final TransactionStatus status) {
                return execution.get();
            }
        });
    }

    @Override
    public void execute(@NonNull final Runnable execution) {
        this.transaction.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                execution.run();
            }
        });
    }

}
