package com.gotsaen.server.event;

import com.gotsaen.server.member.entity.Member;
import lombok.Getter;

@Getter
public class MemberRegistrationApplicationEvent {
    private Member member;

    public MemberRegistrationApplicationEvent(Member member){
        this.member = member;
    }
}
