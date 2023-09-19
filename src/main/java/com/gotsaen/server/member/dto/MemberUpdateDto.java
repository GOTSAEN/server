package com.gotsaen.server.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
@Getter
public class MemberUpdateDto {
    private long memberId;
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
    private String password;

    private String businessName;

    private String businessAddress;
    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }
}
