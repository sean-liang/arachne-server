package arachne.server.domain;

public abstract class AbstractTargetListener implements TargetListener {

    @Override
    public void onCreated(Target target) {

    }

    @Override
    public void onDestroyed(Target target) {

    }

    @Override
    public void onStatusChanged(Target target, TargetStatus previousStatus, String message) {

    }

    @Override
    public void onActionFail(JobAction action, int status) {

    }

    @Override
    public void onActionExpire(JobAction action) {

    }

}
