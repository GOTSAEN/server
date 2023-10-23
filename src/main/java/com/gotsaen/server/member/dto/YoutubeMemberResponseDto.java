package com.gotsaen.server.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.Column;
import javax.validation.constraints.Size;

@AllArgsConstructor
@Getter
public class YoutubeMemberResponseDto {
    private Long youtubeMemberId;
    private String email;
    private String nickname;
    private String avatarUri;
    private String channelId;
    private String category;
}
