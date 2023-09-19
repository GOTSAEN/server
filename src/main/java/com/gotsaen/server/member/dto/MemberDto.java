package com.gotsaen.server.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
public class MemberDto {
    @NotBlank
    @Email
    private String email;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
    private String password;

    @NotBlank
    private String businessName;
    private String businessAddress;

}
