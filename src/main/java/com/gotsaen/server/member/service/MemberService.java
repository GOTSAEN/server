package com.gotsaen.server.member.service;


import com.gotsaen.server.advertisement.dto.AdvertisementByStatusDto;
import com.gotsaen.server.advertisement.dto.AdvertisementSummaryDto;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.mapper.AdvertisementMapper;
import com.gotsaen.server.advertisement.repository.AdvertisementRepository;
import com.gotsaen.server.application.dto.ApplicationAndAdInfoDto;
import com.gotsaen.server.application.dto.ApplicationAndYoutuberInfoDto;
import com.gotsaen.server.application.entity.Application;
import com.gotsaen.server.application.mapper.ApplicationMapper;
import com.gotsaen.server.application.repository.ApplicationRepository;
import com.gotsaen.server.auth.utils.CustomAuthorityUtils;
import com.gotsaen.server.dto.MultiResponseDto;
import com.gotsaen.server.event.MemberRegistrationApplicationEvent;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import com.gotsaen.server.member.dto.MemberPasswordUpdateDto;
import com.gotsaen.server.member.dto.MemberUpdateDto;
import com.gotsaen.server.member.dto.MemberResponseDto;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.entity.YoutubeMember;
import com.gotsaen.server.member.mapper.MemberMapper;
import com.gotsaen.server.member.repository.MemberRepository;
import com.gotsaen.server.member.repository.YoutubeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
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
    private final AdvertisementMapper advertisementMapper;
    private final ApplicationMapper applicationMapper;
    private final YoutubeMemberRepository youtubeMemberRepository;
    @Transactional(propagation = Propagation.REQUIRED)
    public Member createMember(Member member) {
        System.out.println("Creating a new member with email: " + member.getEmail());
        verifyExistsEmail(member.getEmail());

        // 추가: Password 암호화
        String encryptedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(encryptedPassword);

        // 추가: DB에 User Role 저장
        List<String> roles = authorityUtils.createRoles(member.getEmail());
        roles.add("ADVERTISER");
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
    public MemberResponseDto updateMember(String memberEmail, MemberUpdateDto updateDto) {
        Member findMember = findMemberByEmail(memberEmail);
        updateDto.setMemberId(findMember.getMemberId());
        Member updateMember = memberMapper.memberUpdateToMember(updateDto);

        Optional.ofNullable(updateMember.getBusinessName())
                .ifPresent(findMember::setBusinessName);
        Optional.ofNullable(updateMember.getBusinessAddress())
                .ifPresent(findMember::setBusinessAddress);
        memberRepository.save(findMember);

        return memberMapper.memberToMemberResponse(findMember);
    }
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public MemberResponseDto updateMemberPassword(String memberEmail, MemberPasswordUpdateDto updateDto) {
        Member findMember = findMemberByEmail(memberEmail);
        if(!passwordEncoder.matches(updateDto.getCurrentPassword(), findMember.getPassword())){
            throw new BusinessLogicException(ExceptionCode.PASSWORD_NOT_MATCH);
        }

        String encryptedPassword = passwordEncoder.encode(updateDto.getPassword());
        findMember.setPassword(encryptedPassword);

        memberRepository.save(findMember);

        return memberMapper.memberToMemberResponse(findMember);
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

        List<AdvertisementByStatusDto> advertisements = new ArrayList<>();
        advertisements = advertisementsPage.getContent().stream()
                .map(advertisement -> {
                    int numberOfApplicants = 0;
                    if(status == Advertisement.Status.WAITING){
                        numberOfApplicants = applicationRepository.countByAdvertisementId(advertisement.getAdvertisementId());
                    }
                    else if(status == Advertisement.Status.PROGRESS){
                        numberOfApplicants = applicationRepository.countByAdvertisementIdAndStatus(advertisement.getAdvertisementId(), Application.Status.FINISHED);
                    }
                    AdvertisementByStatusDto summaryDto = advertisementMapper.advertisementToAdvertisementByStatusDto(advertisement);
                    summaryDto.setNumberOfApplicants(numberOfApplicants);
                    if (!advertisement.getImageUrlList().isEmpty()) {
                        summaryDto.setImageUrl(advertisement.getImageUrlList().get(0));
                    }
                    return summaryDto;
                })
                .collect(Collectors.toList());

        return new MultiResponseDto<>(advertisements, advertisementsPage);
    }

    public MultiResponseDto findApplicationsByAdvertisementAndMember(String memberEmail, Long advertisementId, int page, int size) {
        Member findMember = findMemberByEmail(memberEmail);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Optional<Advertisement> optionalAdvertisement = advertisementRepository.findById(advertisementId);
        Advertisement advertisement = optionalAdvertisement.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.ADVERTISEMENT_NOT_FOUND));
        Page<Application> applicationsPage = applicationRepository.findByAdvertisementIdAndMemberId(advertisementId, findMember.getMemberId(), pageable);
        List<ApplicationAndYoutuberInfoDto> applications = new ArrayList<>();
        applications =  applicationsPage.getContent().stream()
                .filter(applicationDto ->
                        (advertisement.getStatus() == Advertisement.Status.WAITING &&
                                (applicationDto.getStatus() == Application.Status.WAITING ||
                                        applicationDto.getStatus() == Application.Status.PROGRESS ||
                                        applicationDto.getStatus() == Application.Status.UNSELECTED)) ||
                                (advertisement.getStatus() == Advertisement.Status.PROGRESS &&
                                        (applicationDto.getStatus() == Application.Status.PROGRESS ||
                                                applicationDto.getStatus() == Application.Status.FINISHED ||
                                                applicationDto.getStatus() == Application.Status.REJECTION)) ||
                                (advertisement.getStatus() == Advertisement.Status.FINISHED &&
                                        applicationDto.getStatus() == Application.Status.FINISHED))
                .map(application -> {
                    ApplicationAndYoutuberInfoDto applicationAndYoutuberInfoDto = applicationMapper.applicationToApplicationAndYoutuberInfo(application);
                    Optional<YoutubeMember> optionalYoutubeMember = youtubeMemberRepository.findById(application.getYoutubeMemberId());
                    YoutubeMember youtubeMember = optionalYoutubeMember.orElseThrow(() ->
                            new BusinessLogicException(ExceptionCode.YOUTUBER_NOT_FOUND));
                    applicationAndYoutuberInfoDto.setYoutubeMemberImage(youtubeMember.getAvatarUri());
                    applicationAndYoutuberInfoDto.setYoutubeMemberNickname(youtubeMember.getNickname());
                    return applicationAndYoutuberInfoDto;
                })
                .collect(Collectors.toList());
        return new MultiResponseDto<>(applications, applicationsPage);
    }


    public void checkAdvertiser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                System.out.println("---------------------");
                System.out.println(authority.getAuthority());
                if ("ROLE_YOUTUBER".equals(authority.getAuthority())) {
                    throw new BusinessLogicException(ExceptionCode.INVALID_ADVERTISER_AUTHORIZATION);
                }
            }
        }
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteMember(String memberEmail) {
        Member findMember = findMemberByEmail(memberEmail);

        memberRepository.delete(findMember);
    }
}
