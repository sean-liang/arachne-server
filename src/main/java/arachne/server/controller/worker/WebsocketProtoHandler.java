package arachne.server.controller.worker;

import arachne.server.controller.worker.message.JobMessageConverter;
import arachne.server.controller.worker.message.WorkerProtocol;
import arachne.server.domain.Worker;
import arachne.server.exceptions.BadRequestException;
import arachne.server.exceptions.ResourceNotFoundException;
import arachne.server.job.JobScheduler;
import arachne.server.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;

@Component
public class WebsocketProtoHandler extends BinaryWebSocketHandler {

    @Autowired
    private WorkerService workerService;

    @Autowired
    private JobScheduler scheduler;

    @Override
    protected void handleBinaryMessage(final WebSocketSession session, final BinaryMessage message) throws Exception {
        final Worker worker = this.workerService.getById(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(ResourceNotFoundException::new);
        try {
            final WorkerProtocol.JobFeedbackMessageList msg = WorkerProtocol.JobFeedbackMessageList.parseFrom(message.getPayload().array());
            this.scheduler.feed(worker, JobMessageConverter.toJobFeedbackMessageList(worker.getId(),
                    session.getRemoteAddress().getAddress().toString(),
                    msg));
        } catch (IOException e) {
            throw new BadRequestException("ILLEGAL_REQUEST_BODY");
        }
    }
}
