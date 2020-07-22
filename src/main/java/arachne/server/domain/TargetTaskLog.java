package arachne.server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TargetTaskLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private long timestamp;

    private String action;

    private String detail;

    public static TargetTaskLog now(final String action, final String detail) {
        return new TargetTaskLog(System.currentTimeMillis(), action, detail);
    }

    public static TargetTaskLog now(final String action) {
        return new TargetTaskLog(System.currentTimeMillis(), action, null);
    }

}
