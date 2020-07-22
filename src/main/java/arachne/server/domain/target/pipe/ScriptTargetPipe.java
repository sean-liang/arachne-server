package arachne.server.domain.target.pipe;

import arachne.server.domain.feedback.JobFeedback;
import arachne.server.scripting.Script;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import org.springframework.data.annotation.Transient;

import java.util.Map;
import java.util.stream.Stream;

@JsonTypeName("SCRIPT_PIPE")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScriptTargetPipe extends AbstractTargetPipe {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String content;


    @JsonIgnore
    @Transient
    private transient Script<TargetPipe> script;

    @Override
    public void destroy() {
        if(null != this.script) {
            this.script.close();
        }
    }

    @Override
    public Stream<Object> proceed(final Stream<Object> stream,
                                  final JobFeedback feedback,
                                  final Map<String, Object> context) throws DropFeedbackException {
        if(null != this.script) {
            return this.script.instance().proceed(stream, feedback, context);
        }
        return stream;
    }
}
