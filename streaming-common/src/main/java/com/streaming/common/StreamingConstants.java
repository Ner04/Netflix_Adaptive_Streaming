package com.streaming.common;

public final class StreamingConstants {

    public static final String TASK_QUEUE = "video-processing";

    public static String workflowId(String videoId) {
        return "process-video-" + videoId;
    }

    private StreamingConstants() {}
}
