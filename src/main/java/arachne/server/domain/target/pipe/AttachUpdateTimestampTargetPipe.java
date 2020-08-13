package arachne.server.domain.target.pipe;

import arachne.server.domain.feedback.JobFeedback;
import arachne.server.domain.target.store.IndexOrder;
import arachne.server.domain.target.store.IndexedDocumentTargetStore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.Document;

import java.util.Map;
import java.util.stream.Stream;

@JsonTypeName("ATTACH_UPDATE_TIMESTAMP_PIPE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AttachUpdateTimestampTargetPipe extends AbstractTargetPipe {

    private static final long serialVersionUID = 1L;

    private String field = "updatedAt";

    private boolean indexed = false;

    private boolean ascending = true;

    @Override
    public void initialize() {
        if (this.indexed && this.getTarget().getStore() instanceof IndexedDocumentTargetStore) {
            final IndexedDocumentTargetStore store = this.getTarget().store();
            store.ensureIndex(this.field, this.ascending ? IndexOrder.ASC : IndexOrder.DESC, false);
        }
    }

    @Override
    public void destroy() {
        if (this.indexed && this.getTarget().getStore() instanceof IndexedDocumentTargetStore) {
            final IndexedDocumentTargetStore store = this.getTarget().store();
            store.removeIndex(this.field);
        }
    }

    @Override
    public Stream<Object> proceed(final Stream<Object> stream,
                                  final JobFeedback feedback,
                                  final Map<String, Object> context) throws DropFeedbackException {
        return stream.peek(d -> {
            if (d instanceof Document) {
                ((Document) d).append(this.field, System.currentTimeMillis());
            }
        });
    }
}
