package com.gotsaen.server.member.dto;

import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

@NoArgsConstructor
@Getter
public class MemberPasswordUpdateDto {
    private long memberId;
    private String currentPassword;
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
    private String password;
    public void validatePassword() {
        if (password == null || !password.matches("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")) {
            throw new BusinessLogicException(ExceptionCode.INVALID_PASSWORD);
        }
    }
    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }
}
