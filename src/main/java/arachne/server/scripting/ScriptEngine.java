package arachne.server.scripting;

import java.util.Map;

public interface ScriptEngine {

    <T> Script<T> createObject(String script,
                       Class<T> interfaceOrParent,
                       Map<String, Object> variables,
                       String...executeBefore);

}
