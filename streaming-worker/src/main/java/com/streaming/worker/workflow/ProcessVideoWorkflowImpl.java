package com.streaming.worker.workflow;

import com.streaming.common.ProcessVideoInput;
import com.streaming.common.Resolution;
import com.streaming.common.Resolutions;
import com.streaming.common.temporal.ProcessVideoWorkflow;
import com.streaming.common.temporal.VideoActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.streaming.common.ProcessingStatus.COMPLETED;
import static com.streaming.common.ProcessingStatus.FAILED;
import static com.streaming.common.ProcessingStatus.PROCESSING;

/**
 * Pure orchestration. Must be deterministic: no I/O, no threads, no clocks.
 * Every step is recorded by Temporal, so a crashed worker resumes from history.
 */
public class ProcessVideoWorkflowImpl implements ProcessVideoWorkflow {

    private final VideoActivities activities = Workflow.newActivityStub(
            VideoActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofHours(1))
                    .setHeartbeatTimeout(Duration.ofMinutes(2))
                    .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build())
                    .build());

    @Override
    public String process(ProcessVideoInput in) {
        try {
            activities.updateStatus(in.videoId(), PROCESSING);
            activities.ensureOutputDirectory(in.outputRelativePath());

            // Fan out: schedule every rendition at once; Temporal runs them in parallel.
            List<Promise<String>> jobs = new ArrayList<>();
            for (Resolution r : Resolutions.LADDER) {
                jobs.add(Async.function(activities::transcodeResolution,
                        in.inputRelativePath(), in.outputRelativePath(), r));
            }

            // Fan in: wait for all of them (order is preserved).
            List<String> streamEntries = jobs.stream().map(Promise::get).toList();

            String master = activities.writeMasterPlaylist(in.outputRelativePath(), streamEntries);
            activities.deleteSourceFile(in.inputRelativePath());
            activities.updateStatus(in.videoId(), COMPLETED);
            return master;
        } catch (ActivityFailure e) {
            activities.updateStatus(in.videoId(), FAILED);
            throw e;
        }
    }
}
