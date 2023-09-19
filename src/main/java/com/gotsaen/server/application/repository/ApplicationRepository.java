package com.gotsaen.server.application.repository;

import com.gotsaen.server.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Application findByYoutubeMemberIdAndAdvertisementId(Long youtubeMemberId, Long advertisementId);
}
