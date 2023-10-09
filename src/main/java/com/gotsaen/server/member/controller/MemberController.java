package com.gotsaen.server.member.controller;

import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.dto.MultiResponseDto;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.member.dto.MemberResponseDto;
import com.gotsaen.server.member.dto.MemberUpdateDto;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.mapper.MemberMapper;
import com.gotsaen.server.member.service.MemberService;
import com.gotsaen.server.member.dto.MemberDto;
import com.gotsaen.server.utils.UriCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;


@RestController
@RequestMapping("/members")
@Validated
@Slf4j
@RequiredArgsConstructor
public class MemberController {
    private final static String MEMBER_DEFAULT_URL = "/members";
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    @PostMapping
    public ResponseEntity<Member> postMember(@Valid @RequestBody MemberDto requestBody) {
        Member member = memberMapper.memberPostToMember(requestBody);

        Member createdMember = memberService.createMember(member);
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, createdMember.getMemberId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping
    public ResponseEntity<?> updateMember(Authentication authentication, @RequestBody MemberUpdateDto updateDto) {
        memberService.checkAdvertiser(authentication);

        Member updatedMember = memberService.updateMember(authentication.getPrincipal().toString(), updateDto);
        return ResponseEntity.ok(updatedMember);

    }

    @GetMapping
    public ResponseEntity<?> getMember(Authentication authentication) {
        memberService.checkAdvertiser(authentication);

        MemberResponseDto getMember = memberService.getMember(authentication.getPrincipal().toString());
        return ResponseEntity.ok(getMember);

    }

    @GetMapping("/advertisement")
    public ResponseEntity<MultiResponseDto> getAdvertisementsByMember(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "1000") int size,
            @RequestParam Advertisement.Status status) {
        memberService.checkAdvertiser(authentication);
        MultiResponseDto advertisements = memberService.findAdvertisementByMember(authentication.getPrincipal().toString(), page, size, status);

        return new ResponseEntity<>(advertisements, HttpStatus.OK);
    }

    @GetMapping("/advertisement/{advertisementId}")
    public ResponseEntity<MultiResponseDto> getApplicationByAdvertisementsAndMember(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "1000") int size,
            @PathVariable Long advertisementId) {
        memberService.checkAdvertiser(authentication);
        MultiResponseDto applications = memberService.findApplicationsByAdvertisementAndMember(authentication.getPrincipal().toString(), advertisementId, page, size);

        return new ResponseEntity<>(applications, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity deleteMember(
            Authentication authentication) {
        memberService.checkAdvertiser(authentication);

        memberService.deleteMember(authentication.getPrincipal().toString());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
