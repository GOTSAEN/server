package com.gotsaen.server.advertisement.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Date;

@NoArgsConstructor
@Getter
public class AdvertisementUpdateDto {
    private Long newNumberOfRecruit;
    private Date newEndDate;
    private String newOffer;
    private String newCategory;
    private String newProductDescription;
    private String newPrecaution;
}
