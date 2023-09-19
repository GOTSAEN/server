package com.gotsaen.server.application.service;

import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.repository.AdvertisementRepository;
import com.gotsaen.server.application.dto.ApplicationDto;
import com.gotsaen.server.application.entity.Application;
import com.gotsaen.server.application.repository.ApplicationRepository;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import com.gotsaen.server.member.entity.YoutubeMember;
import com.gotsaen.server.member.repository.YoutubeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final YoutubeMemberRepository youtubeMemberRepository;
    private final AdvertisementRepository advertisementRepository;
    private final ApplicationRepository applicationRepository;
    @Transactional
    public void createOrDeleteBookmark(ApplicationDto requestBody, String email) {
        YoutubeMember youtubeMember = youtubeMemberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        Advertisement advertisement = advertisementRepository.findById(requestBody.getAdvertisementId())
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.ADVERTISEMENT_NOT_FOUND));
        Application existingApplication = applicationRepository
                .findByYoutubeMemberIdAndAdvertisementId(youtubeMember.getYoutubeMemberId(), advertisement.getAdvertisementId());

        if(existingApplication != null){
            applicationRepository.delete(existingApplication);
        }
        else{
            Application application = new Application();
            application.setAdvertisementId(requestBody.getAdvertisementId());
            application.setMemberId(requestBody.getMemberId());
            application.setYoutubeMemberId(youtubeMember.getYoutubeMemberId());
            applicationRepository.save(application);
        }
    }
}
