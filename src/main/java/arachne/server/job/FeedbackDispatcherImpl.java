package arachne.server.job;

import arachne.server.domain.Worker;
import arachne.server.domain.feedback.JobFeedbackMessageList;
import arachne.server.service.TargetService;
import com.cronutils.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeedbackDispatcherImpl implements FeedbackDispatcher {

    @Autowired
    private JobScheduler scheduler;

    @Autowired
    private TargetService targetService;

    @Override
    public void feed(final Worker worker, final JobFeedbackMessageList feedback) {
        if (worker.isManaged()) {
            this.scheduler.feed(worker, feedback);
        } else {
            feedback.getFeedback().forEach(fb -> {
                if(StringUtils.isEmpty(fb.getTargetId())) {
                    log.warn("Drop tracked feedback from unmanaged worker: {}", fb.getId());
                } else {
                    targetService
                            .getById(fb.getTargetId())
                            .ifPresentOrElse(target -> target.feed(fb), () -> log.warn("No such target: {}", fb.getTargetId()));
                }
            });
        }
    }

}
