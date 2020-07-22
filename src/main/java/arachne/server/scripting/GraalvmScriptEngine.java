package arachne.server.scripting;

import lombok.NonNull;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

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
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(line -> line.length() > 0)
                    .map(line -> !line.endsWith(";") ? line + ";\n": line +"\n")
                    .forEach(execution::append);
        }
        execution.append(this.decorateScript(script));
        final Value result = context.eval(LANGUAGE_ID, execution.toString());
        return new GraalvmScript<T>(context, result.as(interfaceOrParent));
    }

    private String createTypeReference(final Class<?> interfaceOrParent) {
        return "var " +
                REF_NAME +
                " = Java.extend(Java.type('" +
                interfaceOrParent.getCanonicalName() +
                "'));\n";
    }

    private String decorateScript(final String script) {
        return "new " +
                REF_NAME +
                "(" +
                script +
                ");\n";
    }
}
