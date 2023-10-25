package com.gotsaen.server.auth.handler;

import com.gotsaen.server.auth.jwt.JwtTokenizer;
import com.gotsaen.server.auth.utils.CustomAuthorityUtils;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import com.gotsaen.server.member.entity.YoutubeMember;
import com.gotsaen.server.member.repository.YoutubeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;


@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final YoutubeMemberRepository youtubeMemberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var oAuth2User = (OAuth2User)authentication.getPrincipal();
        String email = String.valueOf(oAuth2User.getAttributes().get("email"));
        Optional<YoutubeMember> optionalYoutubeMember = youtubeMemberRepository.findByEmail(email);
        YoutubeMember findYoutubeMember =
                optionalYoutubeMember.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        Long id = findYoutubeMember.getYoutubeMemberId();
        List<String> authorities = authorityUtils.createRoles(email);
        authorities.add("YOUTUBER");
        redirect(request, response, email, id, authorities);
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response, String username, Long userid, List<String> authorities) throws IOException {
        String accessToken = delegateAccessToken(username, userid, authorities);
        String refreshToken = delegateRefreshToken(username);
        ResponseCookie accessTokenCookie = ResponseCookie.from("Authorization", "Bearer" + accessToken)
                .maxAge(60 * 60)
                .path("/")
                .secure(false)
                .sameSite("None")
                .httpOnly(false)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("Refresh", refreshToken)
                .maxAge(7 * 24 * 60 * 60)
                .path("/")
                .secure(false)
                .sameSite("None")
                .httpOnly(false)
                .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh", refreshToken);
        response.setHeader("UserType", "youtuber");
        String uri = createURI().toString();
        getRedirectStrategy().sendRedirect(request, response, uri);
    }

    private String delegateAccessToken(String username, Long userid, List<String> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", userid);
        claims.put("email", username);
        claims.put("roles", authorities);

        String subject = username;
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);

        return accessToken;
    }

    private String delegateRefreshToken(String username) {
        String subject = username;
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey);

        return refreshToken;
    }

    private URI createURI() {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        return UriComponentsBuilder
                .newInstance()
                .scheme("https")
                .host("gotsaen.github.io")
                .path("/client")
                .queryParams(queryParams)
                .build()
                .toUri();

//        //로컬용
//        return UriComponentsBuilder
//                .newInstance()
//                .scheme("http")
//                .host("localhost")
//                .path("/receive-token.html")
//                .queryParams(queryParams)
//                .build()
//                .toUri();
    }
}
