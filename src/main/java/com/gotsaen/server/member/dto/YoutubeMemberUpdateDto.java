package com.gotsaen.server.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class YoutubeMemberUpdateDto {
    private Long youtubeMemberId;
    private String channelId;
    private String category;

    public void setYoutubeMemberId(long youtubeMemberId) {
        this.youtubeMemberId = youtubeMemberId;
    }
}
