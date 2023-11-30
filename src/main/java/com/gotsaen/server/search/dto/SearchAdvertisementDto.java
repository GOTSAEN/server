package com.gotsaen.server.search.dto;

import com.gotsaen.server.advertisement.entity.Advertisement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@AllArgsConstructor
@Getter
public class SearchAdvertisementDto {
    private Long advertisementId;
    private String productName;
    private Long numberOfRecruit;
    private String category;

    @Setter
    private int numberOfApplicants;
    @Setter
    private String imageUrl;
}
