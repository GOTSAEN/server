package com.gotsaen.server.advertisement.service;

import com.gotsaen.server.advertisement.dto.AdvertisementUpdateDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.repository.AdvertisementRepository;
import com.gotsaen.server.event.AdvertisementRegistrationApplicationEvent;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    private final ApplicationEventPublisher publisher;

    public AdvertisementService(AdvertisementRepository advertisementRepository, ApplicationEventPublisher publisher) {
        this.advertisementRepository = advertisementRepository;
        this.publisher = publisher;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Advertisement createAdvertisement(Advertisement advertisement) {
        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);

        publisher.publishEvent(new AdvertisementRegistrationApplicationEvent(savedAdvertisement));
        return savedAdvertisement;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public Advertisement updateAdvertisement(Long advertisementId, AdvertisementUpdateDto updateDto) {
        Optional<Advertisement> optionalAdvertisement = advertisementRepository.findById(advertisementId);

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
}
