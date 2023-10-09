package com.gotsaen.server.advertisement.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.sql.Date;

@NoArgsConstructor
public class AdvertisementDto {
    @Getter
    public static class Post {
        @NotBlank
        private String productName;
        private Long numberOfRecruit;
        private Date startDate;
        private Date endDate;
        private String category;
        private String offer;
        private String productDescription;
        private String precaution;
        private String imageUrl;
    }
}
