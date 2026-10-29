package com.streaming.api.video;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private final VideoService service;

    public VideoController(VideoService service) {
        this.service = service;
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadResult>> upload(@RequestParam("video") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No file uploaded"));
        }
        UploadResult result = service.acceptUpload(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok("Video processing started", result));
    }

    @GetMapping
    public ApiResponse<List<VideoDto>> list() {
        return ApiResponse.ok(service.list());
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<ApiResponse<VideoDto>> status(@PathVariable String videoId) {
        return service.find(videoId)
                .map(v -> ResponseEntity.ok(ApiResponse.ok(v)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Video not found")));
    }
}
