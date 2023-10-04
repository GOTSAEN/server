package com.gotsaen.server.auth.service;


import com.gotsaen.server.auth.utils.CustomAuthorityUtils;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import com.gotsaen.server.member.entity.Member;
import com.gotsaen.server.member.entity.YoutubeMember;
import com.gotsaen.server.member.repository.MemberRepository;
import com.gotsaen.server.member.repository.YoutubeMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    private final YoutubeMemberRepository youtubeMemberRepository;

    private final HttpSession httpSession;

    private final CustomAuthorityUtils authorityUtils;
    @Autowired
    public OAuth2MemberService(YoutubeMemberRepository youtubeMemberRepository, HttpSession httpSession, CustomAuthorityUtils authorityUtils) {
        this.youtubeMemberRepository = youtubeMemberRepository;
        this.httpSession = httpSession;
        this.authorityUtils = authorityUtils;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest memberRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(memberRequest);
        String email = oAuth2User.getAttribute("email");

        System.out.println("-------------------------------------");
        System.out.println(oAuth2User.getAttributes());
        System.out.println("-------------------------------------");
        Optional<YoutubeMember> findYoutubeMember = youtubeMemberRepository.findByEmail(email);
        List<String> authorities = authorityUtils.createRoles(email);
        authorities.add("YOUTUBER");
        String nickname = oAuth2User.getAttribute("name");
        String avatarUri = oAuth2User.getAttribute("picture");
        if (findYoutubeMember.isEmpty()) { //찾지 못했다면
            YoutubeMember youtubeMember = YoutubeMember.builder()
                    .nickname(nickname)
                    .email(email)
                    .avatarUri(avatarUri)
                    .roles(authorities).build();
            youtubeMemberRepository.save(youtubeMember);
        }
        return oAuth2User;
    }

    public void checkYoutuber(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                System.out.println("---------------------");
                System.out.println(authority.getAuthority());
                if ("ROLE_ADVERTISER".equals(authority.getAuthority())) {
                    throw new BusinessLogicException(ExceptionCode.INVALID_YOUTUBER_AUTHORIZATION);
                }
            }
        }
    }
}