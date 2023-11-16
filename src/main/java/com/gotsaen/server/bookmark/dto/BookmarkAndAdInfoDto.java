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
    private String adName;
    private String adCategory;
    private Date adEndDate;
    private Long adNumberOfRecruit;
    private String adImage;
}
