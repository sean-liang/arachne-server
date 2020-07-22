package arachne.server.util;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CronExpressionTest {

    @Test
    void testGetTimeAfter() throws ParseException {
        final CronExpression exp = new CronExpression("3 2 1 ? * * *");

        final Date date = exp.getTimeAfter(Date.from(LocalDateTime.of(2020, 1, 1, 1, 15, 0).toInstant(ZoneOffset.ofTotalSeconds(TimeZone.getDefault().getRawOffset() / 1000))));
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH));
        assertEquals(2, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, cal.get(Calendar.HOUR));
        assertEquals(2, cal.get(Calendar.MINUTE));
        assertEquals(3, cal.get(Calendar.SECOND));
    }
}