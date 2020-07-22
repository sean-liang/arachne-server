package arachne.server;

import arachne.server.controller.admin.form.TargetForm;
import arachne.server.domain.*;
import arachne.server.domain.feedback.JobFeedback;
import arachne.server.domain.feedback.JobFeedbackMessageList;
import arachne.server.domain.target.actionprovider.TemplateGeneratedTargetActionProvider;
import arachne.server.domain.target.store.MongoDocumentTargetStore;
import org.assertj.core.util.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class TestDataUtils {

    public static JobAction createJobAction(final String url) {
        return new JobAction(url);
    }

    public static JobFeedbackMessageList createJobFeedbackList(final String workerId, final JobFeedback feedback) {
        return JobFeedbackMessageList.builder().id(workerId).ip(workerId + "-ip").feedback(Lists.newArrayList(feedback)).build();
    }

    public static JobFeedback createJobFeedback(final JobFeedbackContentType type, long id, int status) {
        return new JobFeedback(type, id, status, null, "{'k': 'v'}".getBytes());
    }

    public static Job createJob(final String workerId, final String targetId, final JobAction action) {
        return Job
                .builder()
                .workerId(workerId)
                .targetId(targetId)
                .action(action)
                .build();
    }

    public static Worker createWorker(final String name) {
        return Worker
                .builder()
                .name(name)
                .tags(Arrays.asList("TEST"))
                .token("token-" + name)
                .engine(WorkerEngine.HTTP_PULL)
                .batchSize(10)
                .status(WorkerStatus.DISABLED)
                .build();
    }

    public static TemplateGeneratedTargetActionProvider createTemplateGeneratedTargetActionProvider() {
        return TemplateGeneratedTargetActionProvider
                .builder()
                .template(HttpRequestTemplate.builder().url("http://test/page?id={{}}").build())
                .build();
    }

    public static Target createTarget(final String name, final Worker... workers) {
        return Target
                .builder()
                .name(name)
                .weight(100)
                .status(TargetStatus.DISABLED)
                .retryStrategy(RetryStrategy.NEVER)
                .retries(0)
                .repetition(TargetRepetition.NEVER)
                .repetitionConfig(null)
                .cancelPrevious(true)
                .workers(null != workers && workers.length != 0 ? Arrays.stream(workers).map(Worker::getId).collect(Collectors.toList()) : Collections.emptyList())
                .provider(createTemplateGeneratedTargetActionProvider())
                .store(MongoDocumentTargetStore.builder().collection("testcol").idField("testfld").build())
                .build();
    }

    public static TargetForm createTargetForm(final String name, final Worker... workers) {
        return TargetForm
                .builder()
                .name(name)
                .weight(100)
                .retryStrategy(RetryStrategy.NEVER)
                .retries(0)
                .repetition(TargetRepetition.NEVER)
                .repetitionConfig(null)
                .cancelPrevious(true)
                .workers(null != workers && workers.length != 0 ? Arrays.stream(workers).map(Worker::getId).collect(Collectors.toList()) : Collections.emptyList())
                .provider(createTemplateGeneratedTargetActionProvider())
                .store(MongoDocumentTargetStore.builder().collection("testcol").idField("testfld").build())
                .build();
    }

}
