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

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementMapper advertisementMapper;
    private final ApplicationEventPublisher publisher;

    private final MemberService memberService;
    private final ApplicationService applicationService;


    @Transactional(propagation = Propagation.REQUIRED)
    public Advertisement createAdvertisement(String memberEmail, Advertisement advertisement) {
        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);
        advertisement.setMemberId(memberService.findMemberByEmail(memberEmail).getMemberId());
        publisher.publishEvent(new AdvertisementRegistrationApplicationEvent(savedAdvertisement));
        return savedAdvertisement;
    }

    public AdvertisementResponseDto getAdvertisement(String memberEmail, Long advertisementId) {
        Member member = memberService.findMemberByEmail(memberEmail);
        if (member != null) {
            Advertisement advertisement = advertisementRepository.findByAdvertisementIdAndMemberId(advertisementId, member.getMemberId()).orElse(null);
            if (advertisement != null) {
                return advertisementMapper.advertisementToAdvertisementResponse(advertisement);
            } else {
                throw new BusinessLogicException(ExceptionCode.ADVERTISEMENT_NOT_FOUND);
            }
        } else {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
    }

    @Transactional
    public Advertisement updateAdvertisement(String memberEmail, Long advertisementId, AdvertisementUpdateDto updateDto) {
        Member member = memberService.findMemberByEmail(memberEmail);
        if (member != null) {
            Optional<Advertisement> optionalAdvertisement = advertisementRepository.findByAdvertisementIdAndMemberId(advertisementId, member.getMemberId());
            if (optionalAdvertisement.isPresent()) {
                Advertisement advertisement = optionalAdvertisement.get();
                advertisement.update(
                        updateDto.getNewNumberOfRecruit(),
                        updateDto.getNewEndDate(),
                        updateDto.getNewCategory(),
                        updateDto.getNewOffer(),
                        updateDto.getNewProductDescription(),
                        updateDto.getNewPrecaution()
                );
                return advertisementRepository.save(advertisement);
            } else {
                throw new BusinessLogicException(ExceptionCode.ADVERTISEMENT_NOT_FOUND);
            }
        } else {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
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
        if (member != null) {
            Optional<Advertisement> optionalAdvertisement = advertisementRepository.findByAdvertisementIdAndMemberId(advertisementId, member.getMemberId());
            if (optionalAdvertisement.isPresent()) {
                Advertisement advertisement = optionalAdvertisement.get();
                advertisementRepository.delete(advertisement);
            } else {
                throw new BusinessLogicException(ExceptionCode.ADVERTISEMENT_NOT_FOUND);
            }
        } else {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
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
}
