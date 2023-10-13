package com.gotsaen.server.advertisement.dto;

import com.gotsaen.server.advertisement.entity.Advertisement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.sql.Date;

@AllArgsConstructor
@Getter
public class AdvertisementByStatusDto {
    private Long advertisementId;

    private String productName;

    private Long numberOfRecruit;

    private Date startDate;

    private Date endDate;

    private String category;

    private String offer;

    private String productDescription;

    private String precaution;

    private Long memberId;

    private String imageUrl;

    private Advertisement.Status status;

    @Setter
    private int numberOfApplicants;
}
