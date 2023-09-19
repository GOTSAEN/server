package com.gotsaen.server.advertisement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AdvertisementSummaryDto {
    private String productName;
    private Long numberOfRecruit;
    private String category;
}
