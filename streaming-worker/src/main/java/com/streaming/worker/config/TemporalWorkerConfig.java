package com.streaming.worker.config;

import com.streaming.worker.activity.VideoActivitiesImpl;
import com.streaming.worker.workflow.ProcessVideoWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.streaming.common.StreamingConstants.TASK_QUEUE;

@Configuration
public class TemporalWorkerConfig {

    @Bean(destroyMethod = "shutdown")
    public WorkerFactory workerFactory(WorkflowClient client, VideoActivitiesImpl activities) {
        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE, WorkerOptions.newBuilder()
                .setMaxConcurrentActivityExecutionSize(4) // FFmpeg is CPU-bound: cap parallel encodes
                .build());
        worker.registerWorkflowImplementationTypes(ProcessVideoWorkflowImpl.class);
        worker.registerActivitiesImplementations(activities);

        factory.start(); // starts polling the task queue in background threads
        return factory;
    }
}
