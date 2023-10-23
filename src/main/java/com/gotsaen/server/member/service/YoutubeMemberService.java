package com.gotsaen.server.member.service;

import com.gotsaen.server.application.entity.Application;
import com.gotsaen.server.application.repository.ApplicationRepository;
import com.gotsaen.server.dto.MultiResponseDto;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import com.gotsaen.server.member.dto.YoutubeMemberResponseDto;
import com.gotsaen.server.member.dto.YoutubeMemberUpdateDto;
import com.gotsaen.server.member.entity.YoutubeMember;
import com.gotsaen.server.member.mapper.MemberMapper;
import com.gotsaen.server.member.repository.YoutubeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YoutubeMemberService {
    private final YoutubeMemberRepository youtubeMemberRepository;
    private final ApplicationRepository applicationRepository;
    private final MemberMapper memberMapper;
    public YoutubeMemberResponseDto getYoutubeMember(String email) {
        YoutubeMember youtubeMember = findYoutubeMemberByEmail(email);
        return memberMapper.youtubeMemberToYoutubeMemberResponse(youtubeMember);
    }
    @Transactional(readOnly = true)
    private YoutubeMember findYoutubeMemberByEmail(String email) {
        Optional<YoutubeMember> optionalYoutubeMember =
                youtubeMemberRepository.findByEmail(email);
        return optionalYoutubeMember.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.YOUTUBER_NOT_FOUND));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteMember(String email) {
        YoutubeMember findYoutubeMember = findYoutubeMemberByEmail(email);

        youtubeMemberRepository.delete(findYoutubeMember);
    }

    public MultiResponseDto getYoutubeMembers(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<YoutubeMember> youtubeMemberPage = youtubeMemberRepository.findAll(pageable);
        List<YoutubeMemberResponseDto> youtubeMembers = new ArrayList<>();
        youtubeMembers = youtubeMemberPage.getContent().stream()
                .map(youtubeMember -> {
                    YoutubeMemberResponseDto youtubeMemberResponseDto = memberMapper.youtubeMemberToYoutubeMemberResponse(youtubeMember);
                    return youtubeMemberResponseDto;
                }).collect(Collectors.toList());

        return new MultiResponseDto<>(youtubeMembers, youtubeMemberPage);
    }

    public YoutubeMemberResponseDto getYoutubeMemberById(Long youtubeMemberId) {
        Optional<YoutubeMember> optionalYoutubeMember =
                youtubeMemberRepository.findById(youtubeMemberId);
        YoutubeMember findYoutubeMember = optionalYoutubeMember.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.YOUTUBER_NOT_FOUND));

        return memberMapper.youtubeMemberToYoutubeMemberResponse(findYoutubeMember);
    }

    public MultiResponseDto getYoutubeMembersByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<YoutubeMember> youtubeMemberPage = youtubeMemberRepository.findByCategory(category, pageable);
        List<YoutubeMemberResponseDto> youtubeMembers = new ArrayList<>();
        youtubeMembers = youtubeMemberPage.getContent().stream()
                .map(youtubeMember -> {
                    YoutubeMemberResponseDto youtubeMemberResponseDto = memberMapper.youtubeMemberToYoutubeMemberResponse(youtubeMember);
                    return youtubeMemberResponseDto;
                }).collect(Collectors.toList());
        return new MultiResponseDto<>(youtubeMembers, youtubeMemberPage);
    }

    public YoutubeMember updateYoutubeMember(String youtubeMemberEmail, YoutubeMemberUpdateDto updateDto) {
        YoutubeMember findYoutubeMember = findYoutubeMemberByEmail(youtubeMemberEmail);
        updateDto.setYoutubeMemberId(findYoutubeMember.getYoutubeMemberId());
        YoutubeMember updateYoutubeMember = memberMapper.youtubeUpdateToYoutubeMember(updateDto);
        Optional.ofNullable(updateYoutubeMember.getChannelId())
                .ifPresent(findYoutubeMember::setChannelId);
        Optional.ofNullable(updateYoutubeMember.getCategory())
                .ifPresent(findYoutubeMember::setCategory);

        return youtubeMemberRepository.save(findYoutubeMember);
    }

    public MultiResponseDto findApplicationsByYoutubeMember(String youtubeMemberEmail, int page, int size, Application.Status status) {
        YoutubeMember findYoutubeMember = findYoutubeMemberByEmail(youtubeMemberEmail);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Application> applicationsPage = applicationRepository.findByYoutubeMemberIdAndStatus(findYoutubeMember.getYoutubeMemberId(), status, pageable);

        List<Application> applications = new ArrayList<>();
        applications =  applicationsPage.getContent().stream()
                .collect(Collectors.toList());
        return new MultiResponseDto<>(applications, applicationsPage);
    }
}
