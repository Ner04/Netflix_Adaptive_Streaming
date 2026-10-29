package com.streaming.common.temporal;

import com.streaming.common.ProcessVideoInput;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ProcessVideoWorkflow {

    /** Returns the master playlist path relative to MEDIA_ROOT. */
    @WorkflowMethod
    String process(ProcessVideoInput input);
}
