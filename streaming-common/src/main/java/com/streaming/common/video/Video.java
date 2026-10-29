package com.streaming.common.video;

import com.streaming.common.ProcessingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "videos", indexes = {
        @Index(name = "idx_videos_status", columnList = "processingStatus"),
        @Index(name = "idx_videos_created_at", columnList = "createdAt")
})
public class Video {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String videoId;

    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    protected Video() {}

    public Video(String videoId, String originalFileName) {
        this.videoId = videoId;
        this.originalFileName = originalFileName;
    }

    public UUID getId() { return id; }
    public String getVideoId() { return videoId; }
    public String getOriginalFileName() { return originalFileName; }
    public ProcessingStatus getProcessingStatus() { return processingStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
