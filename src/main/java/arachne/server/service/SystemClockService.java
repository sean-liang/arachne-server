package arachne.server.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SystemClockService {

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public long currentTimeMinutes(final long millis) {
        return TimeUnit.MILLISECONDS.toMinutes(millis);
    }

    public long currentTimeMinutes() {
        return this.currentTimeMinutes(this.currentTimeMillis());
    }

}
