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
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final ApplicationEventPublisher publisher;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorityUtils authorityUtils;

    @Autowired
    public MemberService(MemberRepository memberRepository, MemberMapper memberMapper, ApplicationEventPublisher publisher, PasswordEncoder passwordEncoder, CustomAuthorityUtils authorityUtils) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
        this.publisher = publisher;
        this.passwordEncoder = passwordEncoder;
        this.authorityUtils = authorityUtils;
    }

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

    public MemberResponseDto getMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
        return memberMapper.memberToMemberResponse(member);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public Member updateMember(Long memberId, MemberUpdateDto updateDto) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);

        if (optionalMember.isPresent()) {
            Member existingMember = optionalMember.get();
            //System.out.println("Updating member with ID: " + existingMember);
            existingMember.update(updateDto.getNewPassword(), updateDto.getNewBusinessName(), updateDto.getNewBusinessAddress());

            return memberRepository.save(existingMember);
        } else {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
    }
}
