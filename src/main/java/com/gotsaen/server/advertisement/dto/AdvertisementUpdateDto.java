package com.gotsaen.server.advertisement.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Date;

@NoArgsConstructor
@Getter
public class AdvertisementUpdateDto {
    private String productName;
    private Long numberOfRecruit;
    private Date endDate;
    private String offer;
    private String category;
    private String productDescription;
    private String precaution;
}
