package arachne.server.domain.listener;

import arachne.server.domain.Target;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;

public class TargetEventListener extends AbstractMongoEventListener<Target> {

    private static void injectTargetInstance(final Target target) {
        if (null != target.getProvider()) {
            target.getProvider().setTarget(target);
        }
        if (null != target.getStore()) {
            target.getStore().setTarget(target);
        }
        if (null != target.getPipes()) {
            target.getPipes().forEach(pipe -> pipe.setTarget(target));
        }
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
