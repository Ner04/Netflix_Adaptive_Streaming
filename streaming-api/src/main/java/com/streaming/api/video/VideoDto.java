package com.streaming.api.video;

import com.streaming.common.ProcessingStatus;
import com.streaming.common.video.Video;

import java.time.Instant;

public record VideoDto(String videoId, String originalFileName, String processingStatus,
                       Instant createdAt, String streamUrl) {

    public static VideoDto from(Video v) {
        boolean ready = v.getProcessingStatus() == ProcessingStatus.COMPLETED;
        return new VideoDto(
                v.getVideoId(),
                v.getOriginalFileName(),
                v.getProcessingStatus().name().toLowerCase(),
                v.getCreatedAt(),
                ready ? "/media/output/" + v.getVideoId() + "/master.m3u8" : null);
    }
}
