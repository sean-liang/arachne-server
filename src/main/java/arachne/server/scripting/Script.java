package arachne.server.scripting;

public interface Script<T> {

    T instance();

    void close();

}
