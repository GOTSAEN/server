package com.gotsaen.server.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
@Getter
public class MemberUpdateDto {
    private long memberId;
    private String businessName;
    private String businessAddress;
    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }
}
