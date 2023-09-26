package com.gotsaen.server.member.service;


import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.repository.AdvertisementRepository;
import com.gotsaen.server.application.entity.Application;
import com.gotsaen.server.application.repository.ApplicationRepository;
import com.gotsaen.server.auth.utils.CustomAuthorityUtils;
import com.gotsaen.server.dto.MultiResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final ApplicationEventPublisher publisher;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorityUtils authorityUtils;
    private final AdvertisementRepository advertisementRepository;
    private final ApplicationRepository applicationRepository;

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
                .ifPresent(findMember::setPassword);
        Optional.ofNullable(updateMember.getBusinessName())
                .ifPresent(findMember::setBusinessName);
        Optional.ofNullable(updateMember.getBusinessAddress())
                .ifPresent(findMember::setBusinessAddress);

        return memberRepository.save(findMember);
    }
    @Transactional(readOnly = true)
    public Member findMemberByEmail(String email) {
        Optional<Member> optionalMember =
                memberRepository.findByEmail(email);
        return optionalMember.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    public MultiResponseDto findAdvertisementByMember(String memberEmail, int page, int size, Advertisement.Status status) {
        Member findMember = findMemberByEmail(memberEmail);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Advertisement> advertisementsPage = advertisementRepository.findByStatusAndMemberId(status, findMember.getMemberId(), pageable);

        List<Advertisement> advertisements = new ArrayList<>();
        advertisements = advertisementsPage.getContent().stream()
                .collect(Collectors.toList());

        return new MultiResponseDto<>(advertisements, advertisementsPage);
    }

    public MultiResponseDto findApplicationsByAdvertisementAndMember(String memberEmail, Long advertisementId, int page, int size) {
        Member findMember = findMemberByEmail(memberEmail);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Application> applicationsPage = applicationRepository.findByAdvertisementIdAndMemberId(advertisementId, findMember.getMemberId(), pageable);
        List<Application> applications = new ArrayList<>();
        applications =  applicationsPage.getContent().stream()
                .collect(Collectors.toList());
        return new MultiResponseDto<>(applications, applicationsPage);
    }


}
