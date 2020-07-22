package arachne.server;

import arachne.server.service.SystemClockService;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestSystemClockService extends SystemClockService {

    private long minute = 1;

    public long currentTimeMillis() {
        return minute * 60 * 1000;
    }

    public long currentTimeMinutes() {
        return minute;
    }
}
