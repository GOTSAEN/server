package com.gotsaen.server.advertisement.dto;

import com.gotsaen.server.advertisement.entity.Advertisement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.List;

@AllArgsConstructor
@Getter
public class AdvertisementResponseDto {
    private Long memberId;
    private String productName;
    private Long numberOfRecruit;
    private Date startDate;
    private Date endDate;
    private String category;
    private String offer;
    private String productDescription;
    private String precaution;
    private Advertisement.Status status;
    @Setter
    private List<String> imageUrls;
    @Setter
    int numberOfBookmarks;
}
