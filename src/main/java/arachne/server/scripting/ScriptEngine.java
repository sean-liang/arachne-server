package arachne.server.scripting;

import lombok.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface ScriptEngine {

    static Map<String, ScriptEngine> engines = Collections.unmodifiableMap(Map.of(
            "js", new GraalvmScriptEngine()
    ));

    <T> Script<T> createObject(String script,
                       Class<T> interfaceOrParent,
                       Map<String, Object> variables,
                       String...executeBefore);

    static ScriptEngine engine(@NonNull final String language) {
        return engines.get(language);
    }

}
