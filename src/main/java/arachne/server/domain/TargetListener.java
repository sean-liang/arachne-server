package arachne.server.domain;

public interface TargetListener {

    void onCreated(Target target);

    void onDestroyed(Target target);

    void onStatusChanged(Target target, TargetStatus previousStatus, String message);

    void onActionFail(JobAction action, int status);

    void onActionExpire(JobAction action);

}
