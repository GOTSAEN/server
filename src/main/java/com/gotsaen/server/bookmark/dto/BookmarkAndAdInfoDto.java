package com.gotsaen.server.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class BookmarkAndAdInfoDto {
    private Long bookmarkId;
    private Long memberId;
    private Long advertisementId;
    private Long youtubeMemberId;
    private String productName;
    private String category;
    private Date endDate;
    private Long numberOfRecruit;
    private String imageUrl;
}
