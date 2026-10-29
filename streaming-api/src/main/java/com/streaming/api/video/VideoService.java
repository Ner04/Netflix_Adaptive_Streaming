package com.streaming.api.video;

import com.streaming.api.config.MediaProperties;
import com.streaming.common.ProcessVideoInput;
import com.streaming.common.temporal.ProcessVideoWorkflow;
import com.streaming.common.video.Video;
import com.streaming.common.video.VideoRepository;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.streaming.common.StreamingConstants.TASK_QUEUE;
import static com.streaming.common.StreamingConstants.workflowId;

@Service
public class VideoService {

    private final VideoRepository videos;
    private final WorkflowClient temporal;
    private final MediaProperties media;

    public VideoService(VideoRepository videos, WorkflowClient temporal, MediaProperties media) {
        this.videos = videos;
        this.temporal = temporal;
        this.media = media;
    }

    /** Save the file, record it, kick off the workflow, return immediately. */
    public UploadResult acceptUpload(MultipartFile file) throws IOException {
        String videoId = UUID.randomUUID().toString();
        String ext = Optional.ofNullable(StringUtils.getFilenameExtension(file.getOriginalFilename())).orElse("mp4");
        Path target = media.uploads().resolve(videoId + "." + ext);
        file.transferTo(target); // streamed to disk, never held in the heap

        videos.save(new Video(videoId, file.getOriginalFilename())); // status = PENDING

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowId(workflowId(videoId)) // same video can never be processed twice
                .build();
        ProcessVideoWorkflow workflow = temporal.newWorkflowStub(ProcessVideoWorkflow.class, options);

        ProcessVideoInput input = new ProcessVideoInput(
                videoId, "uploads/" + target.getFileName(), "output/" + videoId);
        WorkflowClient.start(workflow::process, input); // async: does not wait for transcoding

        return new UploadResult(videoId, options.getWorkflowId());
    }

    public Optional<VideoDto> find(String videoId) {
        return videos.findByVideoId(videoId).map(VideoDto::from);
    }

    public List<VideoDto> list() {
        return videos.findAllByOrderByCreatedAtDesc().stream().map(VideoDto::from).toList();
    }
}
