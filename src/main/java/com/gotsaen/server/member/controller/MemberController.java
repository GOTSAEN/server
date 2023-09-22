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
    public ResponseEntity<Member> postMember(@Valid @RequestBody MemberDto requestBody){
        Member member = memberMapper.memberPostToMember(requestBody);

        Member createdMember = memberService.createMember(member);
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, createdMember.getMemberId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping
    public ResponseEntity<?> updateMember(@AuthenticationPrincipal String memberEmail, @RequestBody MemberUpdateDto updateDto) {
        try {
            Member updatedMember = memberService.updateMember(memberEmail, updateDto);
            return ResponseEntity.ok(updatedMember.getMemberId());
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found"); // 또는 적절한 응답을 반환
        }
    }

    @GetMapping
    public ResponseEntity<?> getMember(@AuthenticationPrincipal String memberEmail) {
        try {
            MemberResponseDto getMember = memberService.getMember(memberEmail);
            return ResponseEntity.ok(getMember);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found"); // 또는 적절한 응답을 반환
        }
    }

    @GetMapping("/ad")
    public ResponseEntity<MultiResponseDto> getAdvertisementsByMember(
            @AuthenticationPrincipal String memberEmail,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "1000") int size,
            @RequestParam Advertisement.Status status){
        MultiResponseDto advertisements = memberService.findAdvertisementByMember(memberEmail, page, size, status);

        return new ResponseEntity<>(advertisements,HttpStatus.OK);
    }
}
