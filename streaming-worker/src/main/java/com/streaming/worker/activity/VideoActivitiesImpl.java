package com.streaming.worker.activity;

import com.streaming.common.ProcessingStatus;
import com.streaming.common.Resolution;
import com.streaming.common.temporal.VideoActivities;
import com.streaming.common.video.VideoRepository;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.failure.ApplicationFailure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class VideoActivitiesImpl implements VideoActivities {

    private final VideoRepository videos;
    private final Path mediaRoot;
    private final String ffmpeg;

    public VideoActivitiesImpl(VideoRepository videos,
                               @Value("${media.root}") Path mediaRoot,
                               @Value("${ffmpeg.path:ffmpeg}") String ffmpeg) {
        this.videos = videos;
        this.mediaRoot = mediaRoot.toAbsolutePath().normalize();
        this.ffmpeg = ffmpeg;
    }

    @Override
    public void updateStatus(String videoId, ProcessingStatus status) {
        videos.updateStatus(videoId, status);
    }

    @Override
    public void ensureOutputDirectory(String outputRelativePath) {
        try {
            Files.createDirectories(resolveMediaPath(outputRelativePath));
        } catch (IOException e) {
            throw Activity.wrap(e);
        }
    }

    @Override
    public String transcodeResolution(String inputRelativePath, String outputRelativePath, Resolution r) {
        Path input = resolveMediaPath(inputRelativePath);
        Path variantDir = resolveMediaPath(outputRelativePath).resolve(r.label());
        ActivityExecutionContext ctx = Activity.getExecutionContext();

        List<String> command = List.of(
                ffmpeg, "-y", "-i", input.toString(),
                "-vf", "scale=" + r.width() + ":" + r.height(),
                "-c:v", "libx264", "-b:v", r.bitRateKbps() + "k",
                "-c:a", "aac",
                "-hls_time", "10",
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", variantDir.resolve("segment-%05d.ts").toString(),
                variantDir.resolve("playlist.m3u8").toString());

        try {
            Files.createDirectories(variantDir);
            Process process = new ProcessBuilder(command)
                    .inheritIO() // never leave ffmpeg's output in an undrained pipe: it would block
                    .start();

            // Heartbeat while FFmpeg runs, so Temporal notices a dead worker in minutes, not hours.
            while (!process.waitFor(10, TimeUnit.SECONDS)) {
                ctx.heartbeat(r.label());
            }
            if (process.exitValue() != 0) {
                throw ApplicationFailure.newFailure(
                        "ffmpeg exited with code " + process.exitValue() + " for " + r.label(), "FfmpegError");
            }
        } catch (IOException e) {
            throw Activity.wrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Activity.wrap(e);
        }

        // This rendition's line in the master playlist.
        return "#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%dx%d\n%s/playlist.m3u8"
                .formatted(r.bitRateKbps() * 1000, r.width(), r.height(), r.label());
    }

    @Override
    public String writeMasterPlaylist(String outputRelativePath, List<String> streamEntries) {
        Path master = resolveMediaPath(outputRelativePath).resolve("master.m3u8");
        try {
            Files.writeString(master, "#EXTM3U\n" + String.join("\n", streamEntries) + "\n");
        } catch (IOException e) {
            throw Activity.wrap(e);
        }
        return outputRelativePath + "/master.m3u8";
    }

    @Override
    public void deleteSourceFile(String inputRelativePath) {
        try {
            Files.deleteIfExists(resolveMediaPath(inputRelativePath));
        } catch (IOException e) {
            throw Activity.wrap(e);
        }
    }

    /** Refuses any path that escapes MEDIA_ROOT (path traversal with ".." segments). */
    private Path resolveMediaPath(String relativePath) {
        Path resolved = mediaRoot.resolve(relativePath.replaceFirst("^/+", "")).normalize();
        if (resolved.equals(mediaRoot) || !resolved.startsWith(mediaRoot)) {
            throw ApplicationFailure.newNonRetryableFailure("Invalid media path: " + relativePath, "InvalidPath");
        }
        return resolved;
    }
}
