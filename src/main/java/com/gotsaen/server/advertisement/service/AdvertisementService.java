package com.gotsaen.server.advertisement.service;

import com.gotsaen.server.advertisement.dto.AdvertisementResponseDto;
import com.gotsaen.server.advertisement.dto.AdvertisementSummaryDto;
import com.gotsaen.server.advertisement.dto.AdvertisementUpdateDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.mapper.AdvertisementMapper;
import com.gotsaen.server.advertisement.repository.AdvertisementRepository;
import com.gotsaen.server.application.service.ApplicationService;
import com.gotsaen.server.dto.MultiResponseDto;
import com.gotsaen.server.event.AdvertisementRegistrationApplicationEvent;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;

import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.service.MemberService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementMapper advertisementMapper;
    private final ApplicationEventPublisher publisher;

    private final MemberService memberService;
    private final ApplicationService applicationService;

    private final Logger log = LoggerFactory.getLogger(getClass());


    @Transactional(propagation = Propagation.REQUIRED)
    public Advertisement createAdvertisement(String memberEmail, Advertisement advertisement) {
        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);
        advertisement.setMemberId(memberService.findMemberByEmail(memberEmail).getMemberId());
        publisher.publishEvent(new AdvertisementRegistrationApplicationEvent(savedAdvertisement));
        return savedAdvertisement;
    }

    public AdvertisementResponseDto getAdvertisement(String memberEmail, Long advertisementId) {
        Member member = memberService.findMemberByEmail(memberEmail);
        Advertisement advertisement = getAdvertisementByIdAndMemberId(advertisementId, member);
        return advertisementMapper.advertisementToAdvertisementResponse(advertisement);
    }

    @Transactional
    public Advertisement updateAdvertisement(String memberEmail, Long advertisementId, AdvertisementUpdateDto updateDto) {
        Member member = memberService.findMemberByEmail(memberEmail);
        Advertisement advertisement = getAdvertisementByIdAndMemberId(advertisementId, member);
        Advertisement updatedAdvertisement = advertisementMapper.advertisementUpdateToAdvertisement(updateDto);

        updateAdvertisementIfPresent(updatedAdvertisement.getProductName(), advertisement::setProductName);
        updateAdvertisementIfPresent(updatedAdvertisement.getNumberOfRecruit(), advertisement::setNumberOfRecruit);
        updateAdvertisementIfPresent(updatedAdvertisement.getEndDate(), advertisement::setEndDate);
        updateAdvertisementIfPresent(updatedAdvertisement.getCategory(), advertisement::setCategory);
        updateAdvertisementIfPresent(updatedAdvertisement.getOffer(), advertisement::setOffer);
        updateAdvertisementIfPresent(updatedAdvertisement.getProductDescription(), advertisement::setProductDescription);
        updateAdvertisementIfPresent(updatedAdvertisement.getPrecaution(), advertisement::setPrecaution);

        return advertisementRepository.save(advertisement);
    }

    @Transactional(readOnly = true)
    public MultiResponseDto<AdvertisementSummaryDto> getAllAdvertisementSummaries(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Advertisement> advertisementPage = advertisementRepository.findAll(pageable);

        List<AdvertisementSummaryDto> advertisementSummaries = advertisementPage.getContent().stream()
                .map(advertisement -> {
                    int numberOfApplicants = getAdvertisementApplicationCount(advertisement.getAdvertisementId());
                    AdvertisementSummaryDto summaryDto = advertisementMapper.advertisementToAdvertisementSummaryDto(advertisement);
                    summaryDto.setNumberOfApplicants(numberOfApplicants);
                    return summaryDto;
                })
                .collect(Collectors.toList());

        return new MultiResponseDto<>(advertisementSummaries, advertisementPage);
    }

    @Transactional(readOnly = true)
    public MultiResponseDto<AdvertisementSummaryDto> getAdvertisementsByCategory(String category, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Advertisement> advertisementPage = advertisementRepository.findByCategory(category, pageable);

        List<AdvertisementSummaryDto> advertisementSummaries = advertisementPage.getContent().stream()
                .map(advertisementMapper::advertisementToAdvertisementSummaryDto)
                .collect(Collectors.toList());

        return new MultiResponseDto<>(advertisementSummaries, advertisementPage);
    }

    @Transactional
    public void deleteAdvertisement(String memberEmail, Long advertisementId) {
        Member member = memberService.findMemberByEmail(memberEmail);
        Advertisement advertisement = getAdvertisementByIdAndMemberId(advertisementId, member);
        advertisementRepository.delete(advertisement);
    }

    @Transactional
    public Advertisement updateAdvertisementStatus(String memberEmail, Long advertisementId) {
        Member member = memberService.findMemberByEmail(memberEmail);
        Advertisement advertisement = getAdvertisementByIdAndMemberId(advertisementId, member);

        if (advertisement.getStatus() == Advertisement.Status.WAITING) {
            advertisement.setStatus(Advertisement.Status.PROGRESS);
            advertisementRepository.save(advertisement);
            return advertisement;
        } else {
            throw new BusinessLogicException(ExceptionCode.INVALID_ADVERTISEMENT_STATUS);
        }
    }


    @Scheduled(cron = "0 0 * * * *" /*fixedRate = 60000*/) // 매 정각에 실행
    public void updateAdvertisementStatus() {
        Date currentDate = new Date();
        List<Advertisement> advertisements = advertisementRepository.findByEndDateLessThan(currentDate);
//      log.info("updateAdvertisementStatus() 메서드가 호출되었습니다.");

        for (Advertisement advertisement : advertisements) {
            if (advertisement.getStatus() == Advertisement.Status.PROGRESS) {
                advertisement.setStatus(Advertisement.Status.FINISHED);
                advertisementRepository.save(advertisement);
//              log.info("Advertisement ID {}의 상태를 FINISHED로 변경했습니다.", advertisement.getAdvertisementId());
            }
        }
    }

    public int getAdvertisementApplicationCount(Long advertisementId) {
        Optional<Advertisement> optionalAdvertisement = advertisementRepository.findById(advertisementId);

        if (optionalAdvertisement.isPresent()) {
            Advertisement advertisement = optionalAdvertisement.get();
            return applicationService.getApplicationCountByAdvertisementId(advertisement.getAdvertisementId());
        } else {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
    }

    private Advertisement getAdvertisementByIdAndMemberId(Long advertisementId, Member member) {
        Optional<Advertisement> optionalAdvertisement = advertisementRepository.findByAdvertisementIdAndMemberId(advertisementId, member.getMemberId());
        if (optionalAdvertisement.isPresent()) {
            return optionalAdvertisement.get();
        } else {
            throw new BusinessLogicException(ExceptionCode.ADVERTISEMENT_NOT_FOUND);
        }
    }

    private <T> void updateAdvertisementIfPresent(T newValue, Consumer<T> setter) {
        if (newValue != null) {
            setter.accept(newValue);
        }
    }
}
