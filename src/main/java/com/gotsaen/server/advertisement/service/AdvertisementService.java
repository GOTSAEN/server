package com.gotsaen.server.advertisement.service;

import com.gotsaen.server.advertisement.dto.AdvertisementResponseDto;
import com.gotsaen.server.advertisement.dto.AdvertisementSummaryDto;
import com.gotsaen.server.advertisement.dto.AdvertisementUpdateDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.mapper.AdvertisementMapper;
import com.gotsaen.server.advertisement.repository.AdvertisementRepository;
import com.gotsaen.server.dto.MultiResponseDto;
import com.gotsaen.server.event.AdvertisementRegistrationApplicationEvent;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementMapper advertisementMapper;
    private final ApplicationEventPublisher publisher;

    public AdvertisementService(AdvertisementRepository advertisementRepository, AdvertisementMapper advertisementMapper, ApplicationEventPublisher publisher) {
        this.advertisementRepository = advertisementRepository;
        this.advertisementMapper = advertisementMapper;
        this.publisher = publisher;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Advertisement createAdvertisement(String memberId, Advertisement advertisement) {
        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);
        advertisement.setMemberId(memberId);
        publisher.publishEvent(new AdvertisementRegistrationApplicationEvent(savedAdvertisement));
        return savedAdvertisement;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public Advertisement updateAdvertisement(String memberId, AdvertisementUpdateDto updateDto) {
        Optional<Advertisement> optionalAdvertisement = advertisementRepository.findByMemberId(memberId);

        if (optionalAdvertisement.isPresent()) {
            Advertisement existingAdvertisement = optionalAdvertisement.get();
            existingAdvertisement.update(
                    updateDto.getNewNumberOfRecruit(),
                    updateDto.getNewEndDate(),
                    updateDto.getNewCategory(),
                    updateDto.getNewOffer(),
                    updateDto.getNewProductDescription(),
                    updateDto.getNewPrecaution()
            );

            return advertisementRepository.save(existingAdvertisement);
        } else {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
    }

    public AdvertisementResponseDto getAdvertisement(String memberId) {
        Advertisement advertisement = advertisementRepository.findByMemberId(memberId).orElse(null);
        if (advertisement == null) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
        return advertisementMapper.advertisementToAdvertisementResponse(advertisement);
    }

    @Transactional(readOnly = true)
    public MultiResponseDto<AdvertisementSummaryDto> getAllAdvertisementSummaries(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Advertisement> advertisementPage = advertisementRepository.findAll(pageable);

        List<AdvertisementSummaryDto> advertisementSummaries = advertisementPage.getContent().stream()
                .map(advertisementMapper::advertisementToAdvertisementSummaryDto)
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
    public void deleteAdvertisement(String memberId) {
        Optional<Advertisement> optionalAdvertisement = advertisementRepository.findByMemberId(memberId);

        if (optionalAdvertisement.isPresent()) {
            Advertisement advertisement = optionalAdvertisement.get();
            advertisementRepository.delete(advertisement);
        } else {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
    }
}
