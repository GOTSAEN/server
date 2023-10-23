package com.gotsaen.server.application.repository;

import com.gotsaen.server.application.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Application findByYoutubeMemberIdAndAdvertisementId(Long youtubeMemberId, Long advertisementId);
    Page<Application> findByStatus(Application.Status status, Pageable pageable);
    int countByAdvertisementId(Long advertisementId);

    Page<Application> findByAdvertisementIdAndMemberId(Long advertisementId, Long memberId, Pageable pageable);

    int countByAdvertisementIdAndStatus(Long advertisementId, Application.Status status);

    Page<Application> findByYoutubeMemberIdAndStatus(Long youtubeMemberId, Application.Status status, Pageable pageable);
}
