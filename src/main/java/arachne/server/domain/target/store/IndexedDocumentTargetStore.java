package arachne.server.domain.target.store;

public interface IndexedDocumentTargetStore extends TargetStore {

    void ensureIndex(final String name, final IndexOrder order, final boolean unique);

    void removeIndex(final String name);

}
