package com.gotsaen.server.member.controller;

import com.gotsaen.server.member.dto.UpdateMemberDto;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.mapper.MemberMapper;
import com.gotsaen.server.member.service.MemberService;
import com.gotsaen.server.member.dto.MemberDto;
import com.gotsaen.server.utils.UriCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

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
    public ResponseEntity postMember(@Valid @RequestBody MemberDto.Post requestBody){
        Member member = memberMapper.memberPostToMember(requestBody);

        Member createdMember = memberService.createMember(member);
        URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, createdMember.getMemberId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{memberId}")
    public ResponseEntity<Member> patchMember( @PathVariable Long memberId, @RequestBody UpdateMemberDto updateDto) {
        Optional<Member> updatedMember = memberService.updateMember(memberId, updateDto);
        return updatedMember.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
}
