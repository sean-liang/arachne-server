package arachne.server.domain.listener;

import arachne.server.domain.Target;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;

import java.util.Optional;

public class TargetEventListener extends AbstractMongoEventListener<Target> {

    private static void injectTargetInstance(final Target target) {
        target.withProvider(p -> p.setTarget(target));
        target.withStore(s -> s.setTarget(target));
        target.withEachPipe(p -> p.setTarget(target));
    }

    @Override
    public void onAfterConvert(final AfterConvertEvent<Target> event) {
        injectTargetInstance(event.getSource());
    }

    @Override
    public void onAfterSave(final AfterSaveEvent<Target> event) {
        injectTargetInstance(event.getSource());
    }

}
