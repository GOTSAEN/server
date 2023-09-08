package com.gotsaen.server.auth.service;


import com.gotsaen.server.auth.utils.CustomAuthorityUtils;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;


@Service
public class OAuth2MemberService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    private final HttpSession httpSession;

    private final CustomAuthorityUtils authorityUtils;
    @Autowired
    public OAuth2MemberService(MemberRepository memberRepository, HttpSession httpSession, CustomAuthorityUtils authorityUtils) {
        this.memberRepository = memberRepository;
        this.httpSession = httpSession;
        this.authorityUtils = authorityUtils;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest memberRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(memberRequest);
        String email = oAuth2User.getAttribute("email");

        System.out.println("-------------------------------------");
        System.out.println(oAuth2User.getName());
        System.out.println(oAuth2User.getAttributes());

        System.out.println("-------------------------------------");
        Optional<Member> findMember = memberRepository.findByEmail(email);
        List<String> authorities = authorityUtils.createRoles(email);

        if (findMember.isEmpty()) { //찾지 못했다면
            Member member = Member.builder()
                    .email(email)
                    .password("")
                    .roles(authorities).build();
            memberRepository.save(member);
        }
        return oAuth2User;
    }

}