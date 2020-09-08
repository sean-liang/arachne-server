package arachne.server.domain;

import arachne.server.domain.feedback.JobFeedback;
import arachne.server.domain.target.actionprovider.TargetActionProvider;
import arachne.server.domain.target.pipe.TargetPipe;
import arachne.server.domain.target.store.TargetStore;
import arachne.server.exceptions.BadRequestException;
import arachne.server.exceptions.ServerFailureException;
import arachne.server.util.CronExpression;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Target
 */
@Slf4j
@Document(collection = "sys_targets")
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Target implements DomainEntity {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private int weight;

    @Indexed
    private TargetStatus status;

    private RetryStrategy retryStrategy;

    private int retries;

    private TargetRepetition repetition;

    // minutes if repetition is INTERVAL, cron string if repetition is CRON
    private String repetitionConfig;

    // cancel previous run if not done otherwise skip this run
    @Builder.Default
    private boolean cancelPrevious = true;

    private Set<String> workers;

    private Set<TargetPipe> pipes;

    private TargetActionProvider provider;

    private TargetStore store;

    @Builder.Default
    private long nextRunAt = 0;

    @Indexed
    @CreatedDate
    private long createdAt;

    @Indexed
    @LastModifiedBy
    private long updatedAt;

    @JsonIgnore
    @Version
    private Long version;

    @Transient
    private transient TargetTask currentTask;

    @Builder.Default
    @JsonIgnore
    @Transient
    private transient Set<TargetListener> listeners = new HashSet<>();

    @SuppressWarnings("unchecked")
    public <T extends TargetActionProvider> T provider() {
        return (T) this.provider;
    }

    @SuppressWarnings("unchecked")
    public <T extends TargetStore> T store() {
        return (T) this.store;
    }

    public void withProvider(final Consumer<TargetActionProvider> func) {
        Optional.ofNullable(this.provider).ifPresent(func);
    }

    public void withStore(final Consumer<TargetStore> func) {
        Optional.ofNullable(this.store).ifPresent(func);
    }

    public void withPipes(final Consumer<Collection<TargetPipe>> func) {
        Optional.ofNullable(this.pipes).ifPresent(func);
    }

    public void withListeners(final Consumer<Collection<TargetListener>> func) {
        Optional.ofNullable(this.listeners).ifPresent(func);
    }

    public void withWorkers(final Consumer<Collection<String>> func) {
        Optional.ofNullable(this.workers).ifPresent(func);
    }

    public void withEachPipe(final Consumer<TargetPipe> func) {
        this.withPipes(list -> list.forEach(func));
    }

    public void withEachListener(final Consumer<TargetListener> func) {
        this.withListeners(list -> list.forEach(func));
    }

    public boolean matchWorker(final String workerId) {
        return this.workers != null && this.workers.contains(workerId);
    }

    public void feed(final JobFeedback feedback) {
        final Map<String, Object> context = new HashMap<>();
        Stream<Object> stream = Stream.of(feedback.content());
        if (null != pipes) {
            for (final TargetPipe pipe : this.pipes) {
                try {
                    stream = pipe.proceed(stream, feedback, context);
                } catch (Throwable e) {
                    log.debug("Drop: {}", feedback.getId());
                    return;
                }
            }
        }
        stream.forEach(data -> {
            this.withProvider(p -> p.feed(data, feedback, context));
            this.withStore(s -> s.save(data));
        });

    }

    public void onActionExpire(final JobAction action) {
        this.withEachListener(l -> l.onActionExpire(action));
    }

    public void onActionFail(final JobAction action, final int status) {
        this.withEachListener(l -> l.onActionFail(action, status));
    }

    public synchronized void schedule(final long ts) {
        if (this.getRepetition() == TargetRepetition.NEVER) {
            throw new BadRequestException("ILLEGAL_STATUS");
        }
        this.updateStatus(() -> TargetStatus.SCHEDULED, "SCHEDULE", () -> {
            this.calculateNextRunAt(ts);
            return new Update().set("nextRunAt", this.nextRunAt);
        }, TargetStatus.FAIL, TargetStatus.DONE, TargetStatus.DISABLED);
    }

    public synchronized void start() {
        this.updateStatus(() -> TargetStatus.RUNNING, "START", () -> {
            final Update update = new Update();
            this.withProvider(p -> {
                p.reset();
                update.set("provider", this.provider);
            });
            this.calculateNextRunAt(System.currentTimeMillis());
            return update.set("nextRunAt", this.nextRunAt);
        }, TargetStatus.SCHEDULED, TargetStatus.FAIL, TargetStatus.DONE, TargetStatus.DISABLED);
    }

    public synchronized void stop() {
        this.updateStatus(() ->
                this.repetition == TargetRepetition.NEVER ? TargetStatus.DONE : TargetStatus.SCHEDULED,
                "STOP",
                () -> {
            this.calculateNextRunAt(0);
            return new Update().set("nextRunAt", this.nextRunAt);
        }, TargetStatus.SCHEDULED, TargetStatus.RUNNING, TargetStatus.PAUSED);
    }

    public synchronized void disable() {
        this.updateStatus(() -> TargetStatus.DISABLED, "DISABLE", () -> {
            this.setNextRunAt(0);
            return new Update().set("nextRunAt", this.nextRunAt);
        }, TargetStatus.SCHEDULED, TargetStatus.RUNNING, TargetStatus.PAUSED, TargetStatus.FAIL, TargetStatus.DONE);
    }

    public synchronized void fail(final String reason) {
        this.updateStatus(() -> TargetStatus.FAIL, StringUtils.isEmpty(reason) ? "FAIL" : "FAIL:" + reason, () -> {
            this.setNextRunAt(0);
            return new Update().set("nextRunAt", this.nextRunAt);
        }, TargetStatus.RUNNING);
    }

    public synchronized void pause() {
        this.updateStatus(() -> TargetStatus.PAUSED, "PAUSE", null, TargetStatus.RUNNING);
    }

    public synchronized void resume() {
        this.updateStatus(() -> TargetStatus.RUNNING, "RESUME", null, TargetStatus.PAUSED);
    }

    public synchronized void initialize() {
        this.withProvider(TargetActionProvider::initialize);
        this.withStore(TargetStore::initialize);
        this.withEachPipe(TargetPipe::initialize);
        this.notifyCreated();
    }

    public synchronized void destroy() {
        this.withEachPipe(TargetPipe::destroy);
        Optional.ofNullable(this.store).ifPresent(TargetStore::destroy);
        Optional.ofNullable(this.provider).ifPresent(TargetActionProvider::destroy);
        this.notifyDestroyed();
        this.withListeners(Collection::clear);
    }

    public synchronized void runOnStatus(final TargetStatus expectation, final Consumer<Target> consumer) {
        if (expectation == this.status) {
            consumer.accept(this);
        }
    }

    public boolean checkStatus(boolean throwException, TargetStatus... required) {
        final boolean result = Arrays.asList(required).contains(this.getStatus());
        if (!result && throwException) {
            throw new BadRequestException("ILLEGAL_STATUS: " + this.status.name());
        }
        return result;
    }

    public void notifyCreated() {
        this.withEachListener(l -> l.onCreated(this));
    }

    public void notifyDestroyed() {
        this.withEachListener(l -> l.onDestroyed(this));
    }

    public void notifyStatusChanged(final TargetStatus previousStatus, final String message) {
        this.withEachListener(l -> l.onStatusChanged(this, previousStatus, message));
    }

    private void calculateNextRunAt(long ts) {
        if (ts > 0) {
            this.nextRunAt = ts;
        } else if (this.repetition == TargetRepetition.CRON) {
            try {
                this.nextRunAt = CronExpression.getTimeAfter(this.repetitionConfig).orElse(0L);
            } catch (Exception ex) {
                throw new ServerFailureException("ILLEGAL_CRON_PATTERN: " + ex.getMessage());
            }
        } else if (this.repetition == TargetRepetition.INTERVAL) {
            final long t = this.getCurrentTask() != null && this.getCurrentTask().getEndTime() > 0 ?
                    this.getCurrentTask().getEndTime() :
                    this.createdAt;
            this.nextRunAt = t + Long.parseLong(this.getRepetitionConfig()) * 60000;
        } else if (this.repetition == TargetRepetition.NEVER) {
            this.nextRunAt = 0;
        }
    }

    private void updateStatus(final Supplier<TargetStatus> expected,
                              final String message,
                              final Supplier<Update> additionalUpdate,
                              final TargetStatus... validPreviousStatus) {
        final TargetStatus expectedStatus = expected.get();
        if (this.status == expectedStatus) {
            return;
        }
        this.checkStatus(true, validPreviousStatus);
        final TargetStatus previousStatus = this.getStatus();
        this.setStatus(expectedStatus);
        Update update = null == additionalUpdate ? new Update() : additionalUpdate.get();
        this.updateFirstById(update.set("status", this.status));
        this.notifyStatusChanged(previousStatus, message);
    }

}
