package arachne.server.controller.worker.message;

import arachne.server.controller.worker.message.WorkerProtocol.JobMessage;
import arachne.server.controller.worker.message.WorkerProtocol.JobMessageList;
import arachne.server.domain.HttpHeader;
import arachne.server.domain.Job;
import arachne.server.domain.JobAction;
import arachne.server.domain.JobFeedbackContentType;
import arachne.server.domain.feedback.JobFeedback;
import arachne.server.domain.feedback.JobFeedbackMessageList;

import java.util.List;
import java.util.stream.Collectors;

public class JobMessageConverter {

    public static JobMessageList toMessageList(final List<Job> jobs) {
        final JobMessageList.Builder builder = JobMessageList.newBuilder();

        final int len = jobs == null ? 0 : jobs.size();
        for (int i = 0; i < len; i++) {
            builder.setJobs(i, toMessage(jobs.get(i)));
        }

        return builder.build();
    }

    public static JobMessage toMessage(final Job job) {
        return JobMessage.newBuilder().setId(job.getId()).setRetries(job.getRetries())
                .setAction(toMessageAction(job.getAction())).build();
    }

    public static JobMessage.JobAction toMessageAction(final JobAction action) {
        final JobMessage.JobAction.Builder builder = JobMessage.JobAction.newBuilder()
                .setMethod(JobMessage.JobAction.HttpMethod.forNumber(action.getMethod().getValue()))
                .setUrl(action.getUrl()).setBody(action.getBody());

        final int len = action.getHeaders() == null ? 0 : action.getHeaders().size();
        for (int i = 0; i < len; i++) {
            builder.setHeaders(i, toMessageActionHeader(action.getHeaders().get(i)));
        }

        return builder.build();
    }

    public static JobMessage.JobAction.HttpHeader toMessageActionHeader(final HttpHeader header) {
        return JobMessage.JobAction.HttpHeader.newBuilder().setKey(header.getKey()).setValue(header.getValue()).build();
    }

    public static JobFeedback toJobFeedbackMessage(final WorkerProtocol.JobFeedbackMessage feedback) {
        return new JobFeedback(
                JobFeedbackContentType.valueOf(feedback.getContentTypeValue()),
                feedback.getId(),
                feedback.getStatus(),
                feedback.getMeta(),
                feedback.getContent().toByteArray());
    }

    public static JobFeedbackMessageList toJobFeedbackMessageList(final String workerId,
                                                                  final String ip,
                                                                  final WorkerProtocol.JobFeedbackMessageList list) {
        return JobFeedbackMessageList
                .builder()
                .id(workerId)
                .ip(ip)
                .feedback(list
                        .getFeedbackList()
                        .stream()
                        .map(JobMessageConverter::toJobFeedbackMessage)
                        .collect(Collectors.toList()))
                .build();
    }
}
