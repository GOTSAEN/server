package com.gotsaen.server.member.controller;

import com.gotsaen.server.advertisement.dto.AdvertisementSummaryDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.application.entity.Application;
import com.gotsaen.server.auth.service.OAuth2MemberService;
import com.gotsaen.server.dto.MultiResponseDto;
import com.gotsaen.server.dto.PageInfo;
import com.gotsaen.server.member.dto.MemberUpdateDto;
import com.gotsaen.server.member.dto.YoutubeMemberResponseDto;
import com.gotsaen.server.member.dto.YoutubeMemberUpdateDto;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.entity.YoutubeMember;
import com.gotsaen.server.member.service.YoutubeMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
    @PatchMapping
    public ResponseEntity<?> updateMember(Authentication authentication, @RequestBody YoutubeMemberUpdateDto updateDto) {
        oAuth2MemberService.checkYoutuber(authentication);
        YoutubeMember updateYoutubeMember = youtubeMemberService.updateYoutubeMember(authentication.getPrincipal().toString(), updateDto);
        return ResponseEntity.ok(updateYoutubeMember);

    }

    @GetMapping("/{youtubeMemberId}")
    public ResponseEntity<?> getYoutubeMemberById(@PathVariable Long youtubeMemberId){
        YoutubeMemberResponseDto getYoutubeMember = youtubeMemberService.getYoutubeMemberById(youtubeMemberId);

        return ResponseEntity.ok(getYoutubeMember);
    }

    @GetMapping("/all")
    public ResponseEntity<MultiResponseDto> getYoutubeMembers(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size){
        MultiResponseDto youtubeMembers = youtubeMemberService.getYoutubeMembers(page, size);

        return new ResponseEntity<>(youtubeMembers, HttpStatus.OK);
    }

    @GetMapping("/byCategory")
    public ResponseEntity<MultiResponseDto> getYoutubeMembersByCategory(
            @RequestParam(name = "category") String category,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        MultiResponseDto youtubeMembers = youtubeMemberService.getYoutubeMembersByCategory(category, page, size);

        return new ResponseEntity<>(youtubeMembers, HttpStatus.OK);
    }

    @GetMapping("/application")
    public ResponseEntity<MultiResponseDto> getApplicationByYoutubeMember(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "1000") int size,
            @RequestParam Application.Status status) {
        oAuth2MemberService.checkYoutuber(authentication);
        MultiResponseDto applications = youtubeMemberService.findApplicationsByYoutubeMember(authentication.getPrincipal().toString(), page, size, status);

        return new ResponseEntity<>(applications, HttpStatus.OK);
    }
    @DeleteMapping
    public ResponseEntity deleteMember(
            Authentication authentication) {
        oAuth2MemberService.checkYoutuber(authentication);

        youtubeMemberService.deleteMember(authentication.getPrincipal().toString());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
