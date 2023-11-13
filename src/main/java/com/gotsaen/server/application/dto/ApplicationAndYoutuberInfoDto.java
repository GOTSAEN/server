package com.gotsaen.server.application.dto;

import com.gotsaen.server.application.entity.Application;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class ApplicationAndYoutuberInfoDto {
    private Long applicationId;
    private Long memberId;
    private Long advertisementId;
    private Long youtubeMemberId;
    private String youtubeUrl;
    private Application.Status status;
    private Date createdAt;
    private Date lastModifiedAt;
    private String youtubeMemberImage;
    private String youtubeMemberNickname;
}
