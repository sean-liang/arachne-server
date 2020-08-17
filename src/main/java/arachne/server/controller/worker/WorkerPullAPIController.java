package arachne.server.controller.worker;

import arachne.server.controller.worker.message.JobMessageConverter;
import arachne.server.controller.worker.message.WorkerProtocol;
import arachne.server.controller.worker.message.WorkerProtocol.JobMessageList;
import arachne.server.domain.Worker;
import arachne.server.exceptions.BadRequestException;
import arachne.server.exceptions.ResourceNotFoundException;
import arachne.server.exceptions.ServerFailureException;
import arachne.server.job.JobScheduler;
import arachne.server.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/worker/pull")
public class WorkerPullAPIController {

    @Autowired
    private WorkerService workerService;

    @Autowired
    private JobScheduler scheduler;

    @GetMapping("/current")
    public Worker current(final Principal principal) {
        return this.workerService.getById(principal.getName()).orElseThrow(ResourceNotFoundException::new);
    }

    @GetMapping("/jobs")
    public void fetchJobs(final Principal principal, final HttpServletResponse response) throws IOException {
        final Worker worker = this.workerService.getById(principal.getName())
                .orElseThrow(ResourceNotFoundException::new);
        this.takeAndWriteJobs(worker, response);
    }

    @PostMapping("/jobs")
    public void pushResults(@RequestParam(name = "n", required = false, defaultValue = "false") boolean fetchNext,
                            final Principal principal, final HttpServletRequest request, final HttpServletResponse response) {
        final Worker worker = this.workerService.getById(principal.getName())
                .orElseThrow(ResourceNotFoundException::new);
        try {
            final WorkerProtocol.JobFeedbackMessageList msg = WorkerProtocol.JobFeedbackMessageList.parseFrom(request.getInputStream());
            this.scheduler.feed(worker, JobMessageConverter.toJobFeedbackMessageList(worker.getId(), request.getRemoteAddr(), msg));
            if (fetchNext) {
                this.takeAndWriteJobs(worker, response);
            }
        } catch (IOException e) {
            throw new BadRequestException("ILLEGAL_REQUEST_BODY");
        }
    }

    private void takeAndWriteJobs(final Worker worker, final HttpServletResponse response) {
        final JobMessageList msg = JobMessageConverter.toMessageList(this.scheduler.take(worker));
        try {
            msg.writeTo(response.getOutputStream());
        } catch (IOException e) {
            throw new ServerFailureException(e.getMessage());
        }
    }

}
