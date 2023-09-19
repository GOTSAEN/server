package com.gotsaen.server.member.service;


import com.gotsaen.server.auth.utils.CustomAuthorityUtils;
import com.gotsaen.server.event.MemberRegistrationApplicationEvent;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import com.gotsaen.server.member.dto.MemberUpdateDto;
import com.gotsaen.server.member.dto.MemberResponseDto;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.mapper.MemberMapper;
import com.gotsaen.server.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final ApplicationEventPublisher publisher;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorityUtils authorityUtils;

    @Transactional(propagation = Propagation.REQUIRED)
    public Member createMember(Member member) {
        System.out.println("Creating a new member with email: " + member.getEmail());
        verifyExistsEmail(member.getEmail());

        // 추가: Password 암호화
        String encryptedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encryptedPassword);

        // 추가: DB에 User Role 저장
        List<String> roles = authorityUtils.createRoles(member.getEmail());
        member.setRoles(roles);

        Member savedMember = memberRepository.save(member);

        publisher.publishEvent(new MemberRegistrationApplicationEvent(savedMember));
        return savedMember;
    }

    private void verifyExistsEmail(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent())
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
    }

    public MemberResponseDto getMember(String memberEmail) {
        Member member = memberRepository.findByEmail(memberEmail).orElse(null);
        if (member == null) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
        return memberMapper.memberToMemberResponse(member);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public Member updateMember(String memberEmail, MemberUpdateDto updateDto) {
        Member findMember = findMemberByEmail(memberEmail);
        updateDto.setMemberId(findMember.getMemberId());
        Member updateMember = memberMapper.memberUpdateToMember(updateDto);

        Optional.ofNullable(updateMember.getPassword())
                .ifPresent(password -> findMember.setPassword(password));
        Optional.ofNullable(updateMember.getBusinessName())
                .ifPresent(businessName -> findMember.setBusinessName(businessName));
        Optional.ofNullable(updateMember.getBusinessAddress())
                .ifPresent(businessAddress -> findMember.setBusinessAddress(businessAddress));

        return memberRepository.save(findMember);
    }
    @Transactional(readOnly = true)
    public Member findMemberByEmail(String email) {
        Optional<Member> optionalMember =
                memberRepository.findByEmail(email);
        Member findMember =
                optionalMember.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        return findMember;
    }
}
