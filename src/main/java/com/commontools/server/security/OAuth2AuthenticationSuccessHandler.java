package com.commontools.server.security;

import com.commontools.server.model.OAuthAccount;
import com.commontools.server.model.User;
import com.commontools.server.repository.OAuthAccountRepository;
import com.commontools.server.service.TokenService;
import com.commontools.server.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final OAuthAccountRepository oauthAccountRepository;
    private final UserService userService;
    private final TokenService tokenService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(OAuthAccountRepository oauthAccountRepository, UserService userService, TokenService tokenService) {
        this.oauthAccountRepository = oauthAccountRepository;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oauthUser.getAttributes();

        String provider = "oauth2";
        if (authentication instanceof OAuth2AuthenticationToken) {
            provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        } else {
            provider = Optional.ofNullable((String) request.getParameter("provider")).orElse("oauth2");
        }
        String providerUserId = Optional.ofNullable(attrs.get("sub"))
                .or(() -> Optional.ofNullable(attrs.get("id")))
                .or(() -> Optional.ofNullable(attrs.get("user_id")))
                .map(String::valueOf)
                .orElse("");

        if (providerUserId.isBlank()) {
            providerUserId = java.util.UUID.randomUUID().toString();
        }

        String email = Optional.ofNullable((String) attrs.get("email"))
                .filter(v -> !v.isBlank())
                .orElse("");
        if (email.isBlank()) {
            email = provider + "_" + providerUserId + "@oauth.local";
        }

        String name = Optional.ofNullable((String) attrs.get("name")).orElse(email);

        // find or create user by email
        Optional<User> userOpt = userService.findByEmail(email);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            User u = new User();
            u.setEmail(email);
            u.setUsername((email.contains("@") ? email.split("@")[0] : provider + "_user") + System.currentTimeMillis()%1000);
            u.setPassword(java.util.UUID.randomUUID().toString());
            u.setEmailVerified(true);
            user = userService.register(u);
        }

        // save oauth account
        Optional<OAuthAccount> oaOpt = oauthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId);
        OAuthAccount oa;
        if (oaOpt.isPresent()) {
            oa = oaOpt.get();
        } else {
            OAuthAccount newAcc = new OAuthAccount();
            newAcc.setProvider(provider);
            newAcc.setProviderUserId(providerUserId);
            newAcc.setUserId(user.getId());
            newAcc.setCreatedAt(LocalDateTime.now());
            oa = oauthAccountRepository.save(newAcc);
        }

        // issue our tokens
        com.commontools.server.dto.AuthResponse authResp = tokenService.createAuthResponse(user);

        // set refresh token as httpOnly cookie
        Cookie cookie = new Cookie("refreshToken", authResp.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath("/");
        cookie.setMaxAge(60*60*24*30); // 30 days
        response.addCookie(cookie);

        // redirect to frontend with access token in fragment
        String redirect = frontendUrl + "/auth/success#access_token=" + authResp.getToken();
        response.sendRedirect(redirect);
    }
}
