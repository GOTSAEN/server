package com.gotsaen.server.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class MemberDto {
    @Getter
    public static class Post {
        @NotBlank
        @Email
        private String email;

        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$")
        private String password;

        @NotBlank
        private String nickname;

        private String avatarUri;

        private String channelId;
    }
}
