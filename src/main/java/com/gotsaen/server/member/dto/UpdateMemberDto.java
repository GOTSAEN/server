package com.gotsaen.server.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
@Getter
@Setter
public class UpdateMemberDto {
    @NotBlank
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
    private String newPassword;

    @NotBlank
    private String newBusinessName;
    private String newBusinessAddress;

}
