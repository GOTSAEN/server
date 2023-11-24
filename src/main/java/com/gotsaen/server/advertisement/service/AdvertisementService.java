package com.gotsaen.server.advertisement.service;

import com.gotsaen.server.advertisement.dto.AdvertisementResponseDto;
import com.gotsaen.server.advertisement.dto.AdvertisementSummaryDto;
import com.gotsaen.server.advertisement.dto.AdvertisementUpdateDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.mapper.AdvertisementMapper;
import com.gotsaen.server.advertisement.repository.AdvertisementRepository;
import com.gotsaen.server.application.entity.Application;
import com.gotsaen.server.application.repository.ApplicationRepository;
import com.gotsaen.server.application.service.ApplicationService;
import com.gotsaen.server.bookmark.entity.Bookmark;
import com.gotsaen.server.bookmark.repository.BookmarkRepository;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    private final ApplicationRepository applicationRepository;
    private final BookmarkRepository bookmarkRepository;


    @Transactional(propagation = Propagation.REQUIRED)
    public Advertisement createAdvertisement(String memberEmail, Advertisement advertisement) {
        // 시작일이 종료일보다 늦을 경우 예외 처리
        if (advertisement.getStartDate().after(advertisement.getEndDate())) {
            throw new BusinessLogicException(ExceptionCode.INVALID_DATE_RANGE);
        }

        // 광고를 저장하기 전에 시작일이 현재 날짜 이후인지 확인
        if (!advertisement.getStartDate().equals(new Date()) && advertisement.getStartDate().before(new Date())) {
            throw new BusinessLogicException(ExceptionCode.INVALID_START_DATE);
        }


        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);
        advertisement.setMemberId(memberService.findMemberByEmail(memberEmail).getMemberId());
        publisher.publishEvent(new AdvertisementRegistrationApplicationEvent(savedAdvertisement));
        return savedAdvertisement;
    }

    public AdvertisementResponseDto getAdvertisement(Long advertisementId) {
        Optional<Advertisement> advertisementOptional = advertisementRepository.findById(advertisementId);

        if (advertisementOptional.isPresent()) {
            Advertisement advertisement = advertisementOptional.get();
            AdvertisementResponseDto responseDto = advertisementMapper.advertisementToAdvertisementResponse(advertisement);

            // 이미지 URL 설정
            if (!advertisement.getImageUrlList().isEmpty()) {
                responseDto.setImageUrls(advertisement.getImageUrlList());
            }

            int numberOfBookmarks = bookmarkRepository.countByAdvertisementId(advertisementId);
            responseDto.setNumberOfBookmarks(numberOfBookmarks);

            return responseDto;
        } else {
            throw new BusinessLogicException(ExceptionCode.ADVERTISEMENT_NOT_FOUND);
        }
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

                    if (!advertisement.getImageUrlList().isEmpty()) {
                        summaryDto.setImageUrl(advertisement.getImageUrlList().get(0));
                    }

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
                .map(advertisement -> {
                    AdvertisementSummaryDto summaryDto = advertisementMapper.advertisementToAdvertisementSummaryDto(advertisement);

                    // 이미지 URL 설정
                    if (!advertisement.getImageUrlList().isEmpty()) {
                        summaryDto.setImageUrl(advertisement.getImageUrlList().get(0));
                    }

                    return summaryDto;
                })
                .collect(Collectors.toList());

        return new MultiResponseDto<>(advertisementSummaries, advertisementPage);
    }


    public MultiResponseDto<AdvertisementSummaryDto> getAdvertisementsByStatus(Advertisement.Status status, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Advertisement> advertisementPage = advertisementRepository.findByStatus(status, pageable);

        List<AdvertisementSummaryDto> advertisementSummaries = advertisementPage.getContent().stream()
                .map(advertisement -> {
                    AdvertisementSummaryDto summaryDto = advertisementMapper.advertisementToAdvertisementSummaryDto(advertisement);

                    // 이미지 URL 설정
                    if (!advertisement.getImageUrlList().isEmpty()) {
                        summaryDto.setImageUrl(advertisement.getImageUrlList().get(0));
                    }

                    return summaryDto;
                })
                .collect(Collectors.toList());

        return new MultiResponseDto<>(advertisementSummaries, advertisementPage);
    }

    @Transactional(readOnly = true)
    public MultiResponseDto<AdvertisementSummaryDto> getAdvertisementsWithNearDeadline(int page, int size) {
        Date currentDate = new Date();
        Date fiveDaysLater = new Date(currentDate.getTime() + TimeUnit.DAYS.toMillis(5));

        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Advertisement> advertisementPage = advertisementRepository.findByEndDateBetween(currentDate, fiveDaysLater, pageable);

        List<AdvertisementSummaryDto> advertisements = advertisementPage.getContent().stream()
                .map(advertisement -> {
                    AdvertisementSummaryDto summaryDto = advertisementMapper.advertisementToAdvertisementSummaryDto(advertisement);

                    // 필요한 경우 다른 속성 설정
                    if (!advertisement.getImageUrlList().isEmpty()) {
                        summaryDto.setImageUrl(advertisement.getImageUrlList().get(0));
                    }

                    return summaryDto;
                })
                .collect(Collectors.toList());

        return new MultiResponseDto<>(advertisements, advertisementPage);
    }

    @Transactional(readOnly = true)
    public MultiResponseDto<AdvertisementSummaryDto> getAdvertisementsWithMostBookmarks(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Advertisement> advertisementPage = advertisementRepository.findAll(pageable);

        List<AdvertisementSummaryDto> advertisementSummaries = advertisementPage.getContent().stream()
                .map(advertisement -> {
                    int numberOfBookmarks = bookmarkRepository.countByAdvertisementId(advertisement.getAdvertisementId());
                    AdvertisementSummaryDto summaryDto = advertisementMapper.advertisementToAdvertisementSummaryDto(advertisement);
                    summaryDto.setNumberOfBookmarks(numberOfBookmarks);

                    if (!advertisement.getImageUrlList().isEmpty()) {
                        summaryDto.setImageUrl(advertisement.getImageUrlList().get(0));
                    }

                    return summaryDto;
                })
                .sorted(Comparator.comparingInt(AdvertisementSummaryDto::getNumberOfBookmarks).reversed())
                .collect(Collectors.toList());

        return new MultiResponseDto<>(advertisementSummaries, advertisementPage);
    }


    @Transactional
    public void deleteAdvertisement(String memberEmail, Long advertisementId) {
        Member member = memberService.findMemberByEmail(memberEmail);
        Advertisement advertisement = getAdvertisementByIdAndMemberId(advertisementId, member);

        // 삭제 전에 연관된 application 및 bookmark을 찾아서 삭제
        List<Application> applications = applicationRepository.findByAdvertisementId(advertisementId);
        List<Bookmark> bookmarks = bookmarkRepository.findByAdvertisementId(advertisementId);

        for (Application application : applications) {
            applicationRepository.delete(application);
        }

        for (Bookmark bookmark : bookmarks) {
            bookmarkRepository.delete(bookmark);
        }

        // Advertisement 삭제
        advertisementRepository.delete(advertisement);
    }

    @Transactional
    public void saveImageUrl(Long advertisementId, String imageUrl) {
        Optional<Advertisement> advertisementOptional = advertisementRepository.findById(advertisementId);

        if (advertisementOptional.isPresent()) {
            Advertisement advertisement = advertisementOptional.get();

            List<String> imageUrlList = advertisement.getImageUrlList();
            imageUrlList.add(imageUrl);
            advertisement.setImageUrlList(imageUrlList);
            advertisementRepository.save(advertisement);

        } else {
            throw new BusinessLogicException(ExceptionCode.ADVERTISEMENT_NOT_FOUND);
        }
    }

    @Scheduled(cron = "0 0 * * * *" /*fixedRate = 60000*/) // 매 정각에 실행
    public void waitingToProgressScheduling() {
        Date currentDate = new Date();
        List<Advertisement> advertisements = advertisementRepository.findByEndDateLessThan(currentDate);

        for (Advertisement advertisement : advertisements) {
            if (advertisement.getStatus() == Advertisement.Status.WAITING) {
                advertisement.setStatus(Advertisement.Status.PROGRESS);
                advertisementRepository.save(advertisement);
            }
        }
    }

    @Transactional
    public Advertisement waitingToProgress(String memberEmail, Long advertisementId) {
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

    @Transactional
    public Advertisement progressToFinished(String memberEmail, Long advertisementId) {
        Member member = memberService.findMemberByEmail(memberEmail);
        Advertisement advertisement = getAdvertisementByIdAndMemberId(advertisementId, member);

        // "REJECTION" 상태의 지원서가 있는지 확인
        List<Application> rejectionApplications = applicationRepository.findByAdvertisementIdAndStatus(advertisementId, Application.Status.REJECTION);
        if (!rejectionApplications.isEmpty()) {
            throw new BusinessLogicException(ExceptionCode.CANNOT_END_WITH_REJECTION);
        }

        if (advertisement.getStatus() == Advertisement.Status.PROGRESS) {
            advertisement.setStatus(Advertisement.Status.FINISHED);
            advertisementRepository.save(advertisement);
            return advertisement;
        } else {
            throw new BusinessLogicException(ExceptionCode.INVALID_ADVERTISEMENT_STATUS);
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
