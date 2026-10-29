package com.streaming.common.temporal;

import com.streaming.common.ProcessingStatus;
import com.streaming.common.Resolution;
import io.temporal.activity.ActivityInterface;

import java.util.List;

/** Every side effect (FFmpeg, disk, database) lives in an activity, never in the workflow. */
@ActivityInterface
public interface VideoActivities {

    void updateStatus(String videoId, ProcessingStatus status);

    void ensureOutputDirectory(String outputRelativePath);

    /** Transcodes one rendition and returns its #EXT-X-STREAM-INF entry for the master playlist. */
    String transcodeResolution(String inputRelativePath, String outputRelativePath, Resolution resolution);

    String writeMasterPlaylist(String outputRelativePath, List<String> streamEntries);

    void deleteSourceFile(String inputRelativePath);
}
