package com.gotsaen.server.auth.filter;

import com.gotsaen.server.auth.jwt.JwtTokenizer;
import com.gotsaen.server.auth.utils.CustomAuthorityUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JwtVerificationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;

    public JwtVerificationFilter(JwtTokenizer jwtTokenizer,
                                 CustomAuthorityUtils authorityUtils) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Map<String, Object> claims = verifyJws(request);
            setAuthenticationToContext(claims);
        } catch (ExpiredJwtException ee) {
            String refreshToken = request.getHeader("Refresh");
            if (refreshToken != null) {
                try {
                    Claims jwtClaims = ee.getClaims();
                    String username = jwtClaims.getSubject();
                    jwtTokenizer.validateRefreshToken(refreshToken, username);
                    Date newExpiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());
                    String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
                    String newAccessToken = jwtTokenizer.generateAccessToken(jwtClaims, username, newExpiration, base64EncodedSecretKey);

                    String newRefreshToken = jwtTokenizer.delegateRefreshToken(username);
                    ResponseCookie accessTokenCookie = ResponseCookie.from("Authorization", "Bearer" + newAccessToken)
                            .maxAge(60 * 60)
                            .path("/")
                            .secure(true)
                            .sameSite("None")
                            .httpOnly(true)
                            .build();

                    ResponseCookie refreshTokenCookie = ResponseCookie.from("Refresh", newRefreshToken)
                            .maxAge(7 * 24 * 60 * 60)
                            .path("/")
                            .secure(true)
                            .sameSite("None")
                            .httpOnly(true)
                            .build();

                    response.addHeader("Set-Cookie", accessTokenCookie.toString());
                    response.addHeader("Set-Cookie", refreshTokenCookie.toString());
                    response.setHeader("Authorization", "Bearer" + newAccessToken);
                    response.setHeader("Refresh", newRefreshToken);
                    if(request.getHeader("userType").equals("advertisement")){
                        response.setHeader("userType", "advertisement");
                    }
                    else{
                        response.setHeader("userType", "youtuber");
                    }

                } catch (ExpiredJwtException e) {
                    throw new ServletException("Refresh token expired");
                }
            }
        } catch (SignatureException se) {
            request.setAttribute("exception", se);
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }
        filterChain.doFilter(request, response);
    }






    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String authorization = request.getHeader("Authorization");

        return authorization == null || !authorization.startsWith("Bearer");
    }

    private Map<String, Object> verifyJws(HttpServletRequest request) {
        String jws = request.getHeader("Authorization").replace("Bearer", "");
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();

        return claims;
    }

    private void setAuthenticationToContext(Map<String, Object> claims) {
        String username = (String) claims.get("email");
        List<GrantedAuthority> authorities = authorityUtils.createAuthorities((List)claims.get("roles"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}