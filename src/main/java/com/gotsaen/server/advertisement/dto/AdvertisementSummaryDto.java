package com.gotsaen.server.advertisement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
public class AdvertisementSummaryDto {
    private Long advertisementId;
    private String productName;
    private Long numberOfRecruit;
    private String category;
    @Setter
    private int numberOfApplicants;
    @Setter
    private String imageUrl;
}
