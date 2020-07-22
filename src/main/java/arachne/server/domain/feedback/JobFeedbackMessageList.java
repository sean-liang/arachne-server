package arachne.server.domain.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class JobFeedbackMessageList implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String ip;

    private List<JobFeedback> feedback;

}
