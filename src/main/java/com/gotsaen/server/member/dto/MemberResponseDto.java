package com.gotsaen.server.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.Email;

@AllArgsConstructor
@Getter
public class MemberResponseDto {
    @Email
    private String email;
    private String businessName;
    private String businessAddress;

}
