package com.gotsaen.server.member.service;


import com.gotsaen.server.auth.utils.CustomAuthorityUtils;
import com.gotsaen.server.event.MemberRegistrationApplicationEvent;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import com.gotsaen.server.member.dto.MemberDto;
import com.gotsaen.server.member.dto.UpdateMemberDto;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.repository.MemberRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;


@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher publisher;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorityUtils authorityUtils;

    public MemberService(MemberRepository memberRepository, ApplicationEventPublisher publisher, PasswordEncoder passwordEncoder, CustomAuthorityUtils authorityUtils) {
        this.memberRepository = memberRepository;
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

    @Transactional
    public Optional<Member> updateMember(Long memberId, UpdateMemberDto updateMemberDto) {
        Optional<Member> updateMember = memberRepository.findByMemberId(memberId);

        if (updateMember.isPresent()) {
            Member existingMember = updateMember.get();

            String newPassword = updateMemberDto.getNewPassword();
            String newBusinessName = updateMemberDto.getNewBusinessName();
            String newBusinessAddress = updateMemberDto.getNewBusinessAddress();

            if (newPassword != null) {
                String encryptedPassword = passwordEncoder.encode(newPassword);
                existingMember.setPassword(encryptedPassword);
            }
            if (newBusinessName != null) {
                existingMember.setBusinessName(newBusinessName);
            }
            if (newBusinessAddress != null) {
                existingMember.setBusinessAddress(newBusinessAddress);
            }

            return Optional.of(memberRepository.save(existingMember));
        } else {
            return Optional.empty();
        }
    }
}
