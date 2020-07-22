package arachne.server.scripting;

import lombok.NonNull;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
public class GraalvmScriptEngine implements ScriptEngine {

    private static final String LANGUAGE_ID = "js";

    private static final String REF_NAME = "_$TYPE_REF$_";

    private final Engine engine = Engine.create();

    @Override
    public <T> Script<T> createObject(@NonNull  final String script,
                        @NonNull final Class<T> interfaceOrParent,
                        final Map<String, Object> variables,
                        final String... executeBefore) {
        final Context context = Context.newBuilder(LANGUAGE_ID).engine(this.engine).allowAllAccess(true).build();
        if(variables!=null) {
            variables.forEach((k,v)-> context.getBindings(LANGUAGE_ID).putMember(k, v));
        }
        final StringBuilder execution = new StringBuilder(this.createTypeReference(interfaceOrParent));
        if(null != executeBefore) {
            Arrays.stream(executeBefore)
                    .filter(line -> null != line)
                    .map(String::trim)
                    .filter(line -> line.length() > 0)
                    .map(line -> !line.endsWith(";") ? line + ";\n": line +"\n")
                    .forEach(line -> execution.append(line));
        }
        execution.append(this.decorateScript(script));
        final Value result = context.eval(LANGUAGE_ID, execution.toString());
        return new GraalvmScript(context, result.as(interfaceOrParent));
    }

    private String createTypeReference(final Class<?> interfaceOrParent) {
        final StringBuilder builder = new StringBuilder();
        builder.append("var ")
                .append(REF_NAME)
                .append(" = Java.extend(Java.type('")
                .append(interfaceOrParent.getCanonicalName())
                .append("'));\n");
        return builder.toString();
    }

    private String decorateScript(final String script) {
        final StringBuilder builder = new StringBuilder();
        builder.append("new ")
                .append(REF_NAME)
                .append("(")
                .append(script)
                .append(");\n");
        return builder.toString();
    }
}
