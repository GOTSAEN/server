package com.gotsaen.server.advertisement.service;

import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.repository.AdvertisementRepository;
import com.gotsaen.server.event.AdvertisementRegistrationApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
}
