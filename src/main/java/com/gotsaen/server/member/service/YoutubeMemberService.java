package com.gotsaen.server.member.service;

import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import com.gotsaen.server.member.dto.YoutubeMemberResponseDto;
import com.gotsaen.server.member.entity.YoutubeMember;
import com.gotsaen.server.member.mapper.MemberMapper;
import com.gotsaen.server.member.repository.YoutubeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class YoutubeMemberService {
    private final YoutubeMemberRepository youtubeMemberRepository;
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
}
