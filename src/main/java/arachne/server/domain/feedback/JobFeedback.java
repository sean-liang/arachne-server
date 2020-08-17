package arachne.server.domain.feedback;

import arachne.server.domain.JobFeedbackContentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.Document;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@Data
@AllArgsConstructor
public class JobFeedback implements Serializable {

    private static final long serialVersionUID = 1L;

    private final JobFeedbackContentType type;

    private final long id;
    private String targetId;
    private final int status;
    private final String meta;
    private final byte[] content;

    public boolean isSuccess() {
        return this.getStatus() == 200;
    }

    public Object content() {
        if (JobFeedbackContentType.BINARY == this.type) {
            return this.content;
        } else if (JobFeedbackContentType.TEXT == this.type) {
            return this.toText();
        } else {
            return Document.parse(this.toText());
        }
    }

    private String toText() {
        return new String(this.content, StandardCharsets.UTF_8);
    }

}
