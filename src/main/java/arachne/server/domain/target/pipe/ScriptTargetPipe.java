package arachne.server.domain.target.pipe;

import arachne.server.domain.feedback.JobFeedback;
import arachne.server.exceptions.BadRequestException;
import arachne.server.scripting.Script;
import arachne.server.scripting.ScriptEngine;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import org.graalvm.polyglot.PolyglotException;
import org.springframework.data.annotation.Transient;

import java.util.Map;
import java.util.stream.Stream;

@JsonTypeName("SCRIPT_PIPE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScriptTargetPipe extends AbstractTargetPipe implements AutoCloseable {

    private static final long serialVersionUID = 1L;

    private static final String SCRIPT_HELPERS = "var DropFeedbackException  = Java.type('" +
            DropFeedbackException.class.getCanonicalName() +
            "');\n";

    private String content;

    @JsonIgnore
    @Transient
    private transient Script<AbstractTargetPipe> script;

    @JsonIgnore
    @Transient
    private transient boolean closed = false;

    @Override
    public synchronized void destroy() {
        this.closed = true;
        if(null != this.script) {
            this.script.close();
            this.script = null;
        }
    }

    @Override
    public synchronized Stream<Object> proceed(final Stream<Object> stream,
                                  final JobFeedback feedback,
                                  final Map<String, Object> context) throws Throwable {
        if(this.closed) {
            throw new BadRequestException("Pipe closed.");
        }
        if(null == this.script) {
            this.script = ScriptEngine
                    .engine("js")
                    .createObject(this.content, AbstractTargetPipe.class, null, SCRIPT_HELPERS);
        }

        try {
            return this.script.instance().proceed(stream, feedback, context);
        } catch(PolyglotException ex) {
            final Throwable t = ex.asHostException();
            if(null != t) {
                throw t;
            } else {
                throw ex;
            }
        }
    }

    @Override
    public void close() throws Exception {
        this.destroy();
    }
}
