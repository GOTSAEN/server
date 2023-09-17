package com.gotsaen.server.member.controller;

import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.member.dto.MemberResponseDto;
import com.gotsaen.server.member.dto.MemberUpdateDto;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.mapper.MemberMapper;
import com.gotsaen.server.member.service.MemberService;
import com.gotsaen.server.member.dto.MemberDto;
import com.gotsaen.server.utils.UriCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;


@RestController
@RequestMapping("/members")
@Validated
@Slf4j
public class MemberController {
    private final static String MEMBER_DEFAULT_URL = "/members";
    private final MemberService memberService;
    private final MemberMapper memberMapper;

    public MemberController(MemberService memberService, MemberMapper memberMapper) {
        this.memberService = memberService;
        this.memberMapper = memberMapper;
    }

    @PostMapping
    public ResponseEntity<Member> postMember(@Valid @RequestBody MemberDto.Post requestBody){
        Member member = memberMapper.memberPostToMember(requestBody);

        Member createdMember = memberService.createMember(member);
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, createdMember.getMemberId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<?> updateMember(@PathVariable Long memberId, @RequestBody MemberUpdateDto updateDto) {
        try {
            Member updatedMember = memberService.updateMember(memberId, updateDto);
            return ResponseEntity.ok(updatedMember);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found"); // 또는 적절한 응답을 반환
        }
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<?> getMember(@PathVariable Long memberId) {
        try {
            MemberResponseDto getMember = memberService.getMember(memberId);
            return ResponseEntity.ok(getMember);
        } catch (BusinessLogicException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found"); // 또는 적절한 응답을 반환
        }
    }
}
