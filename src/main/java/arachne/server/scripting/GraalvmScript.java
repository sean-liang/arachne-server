package arachne.server.scripting;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.graalvm.polyglot.Context;

@AllArgsConstructor
public class GraalvmScript<T> implements Script<T> {

    @NonNull
    private final Context context;

    @NonNull
    private final T instance;

    @Override
    public T instance() {
        return this.instance;
    }

    @Override
    public void close() {
        this.context.close(true);
    }
}
