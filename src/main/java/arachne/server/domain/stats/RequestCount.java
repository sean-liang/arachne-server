package arachne.server.domain.stats;

import lombok.Data;

@Data
public class RequestCount {

    private int successCount = 0;

    private int failCount = 0;

    public RequestCount add(final int success, final int fail) {
        this.successCount += success;
        this.failCount += fail;
        return this;
    }

}
