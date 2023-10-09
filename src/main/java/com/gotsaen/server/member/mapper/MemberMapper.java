package com.gotsaen.server.member.mapper;

import com.gotsaen.server.member.dto.MemberDto;
import com.gotsaen.server.member.dto.MemberResponseDto;
import com.gotsaen.server.member.dto.MemberUpdateDto;
import com.gotsaen.server.member.dto.YoutubeMemberResponseDto;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.entity.YoutubeMember;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {
    Member memberPostToMember(MemberDto requestBody);
    MemberResponseDto memberToMemberResponse(Member member);
    Member memberUpdateToMember(MemberUpdateDto requestBody);
    YoutubeMemberResponseDto youtubeMemberToYoutubeMemberResponse(YoutubeMember youtubeMember);
}