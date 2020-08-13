package arachne.server.util;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CronExpressionTest {

    @Test
    void testGetTimeAfter() throws ParseException {
        val nextExecutionTs = CronExpression.getTimeAfter("2 1 * * *",
                LocalDateTime.of(2020, 1, 1, 1, 15, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        assertTrue(nextExecutionTs.isPresent());

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(nextExecutionTs.get());
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH));
        assertEquals(2, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, cal.get(Calendar.HOUR));
        assertEquals(2, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }
}