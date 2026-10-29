package com.streaming.common.video;

import com.streaming.common.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {

    Optional<Video> findByVideoId(String videoId);

    List<Video> findAllByOrderByCreatedAtDesc();

    @Transactional
    @Modifying
    @Query("update Video v set v.processingStatus = :status, v.updatedAt = CURRENT_TIMESTAMP where v.videoId = :videoId")
    int updateStatus(@Param("videoId") String videoId, @Param("status") ProcessingStatus status);
}
