package com.gotsaen.server.advertisement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Date;

@AllArgsConstructor
@Getter
public class AdvertisementResponseDto {
    private String productName;
    private Long numberOfRecruit;
    private Date startDate;
    private Date endDate;
    private String category;
    private String offer;
    private String productDescription;
    private String precaution;
}
