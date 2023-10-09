package com.gotsaen.server.member.controller;

import com.gotsaen.server.auth.service.OAuth2MemberService;
import com.gotsaen.server.member.dto.MemberUpdateDto;
import com.gotsaen.server.member.dto.YoutubeMemberResponseDto;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.service.YoutubeMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/youtubers")
@Validated
@Slf4j
@RequiredArgsConstructor
public class YoutubeMemberController {
    private final OAuth2MemberService oAuth2MemberService;
    private final YoutubeMemberService youtubeMemberService;
    @GetMapping("/me")
    public ResponseEntity<?> getYoutubeMember(Authentication authentication){
        oAuth2MemberService.checkYoutuber(authentication);
        YoutubeMemberResponseDto getYoutubeMember = youtubeMemberService.getYoutubeMember(authentication.getPrincipal().toString());

        return ResponseEntity.ok(getYoutubeMember);
    }

    @DeleteMapping
    public ResponseEntity deleteMember(
            Authentication authentication) {
        oAuth2MemberService.checkYoutuber(authentication);

        youtubeMemberService.deleteMember(authentication.getPrincipal().toString());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
