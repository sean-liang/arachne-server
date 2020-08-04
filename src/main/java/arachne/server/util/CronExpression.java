package arachne.server.util;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Cron expression utils.
 */
public class CronExpression {

    private static final CronDefinition CRON_DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);

    private static final CronParser CRON_PARSER = new CronParser(CRON_DEFINITION);

    /**
     * Calculate the next execution timestamp after now
     *
     * @param cronExpression unix pattern cron expressions
     * @return next execution timestamp after now
     */
    public static Optional<Long> getTimeAfter(final String cronExpression) {
        return getTimeAfter(cronExpression, null);
    }

    /**
     * Calculate the next execution timestamp after the given timestamp
     *
     * @param cronExpression unix pattern cron expressions
     * @param afterThisTimestamp given timestamp
     * @return next execution timestamp after the given timestamp
     */
    public static Optional<Long> getTimeAfter(final String cronExpression, final Long afterThisTimestamp) {
        final Cron cron = CRON_PARSER.parse(cronExpression);
        final ExecutionTime executionTime = ExecutionTime.forCron(cron);
        final ZonedDateTime afterDateTime = null == afterThisTimestamp ?
                ZonedDateTime.now() :
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(afterThisTimestamp), ZoneId.systemDefault());
        final Optional<ZonedDateTime> nextExecutionTime = executionTime.nextExecution(afterDateTime);
        return nextExecutionTime.map(dt -> dt.toInstant().toEpochMilli());
    }

}
