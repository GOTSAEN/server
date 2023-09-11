package com.gotsaen.server.event;

import com.gotsaen.server.advertisement.entity.Advertisement;
import lombok.Getter;

@Getter

public class AdvertisementRegistrationApplicationEvent {
    private Advertisement advertisement;

    public AdvertisementRegistrationApplicationEvent(Advertisement advertisement){
        this.advertisement = advertisement;
    }
}
