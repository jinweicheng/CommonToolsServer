package com.commontools.server.service;

import com.commontools.server.dto.AuthResponse;
import com.commontools.server.dto.LoginRequest;
import com.commontools.server.dto.RegisterRequest;
import com.commontools.server.model.User;
import com.commontools.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());

        User savedUser = userService.register(user);
        String token = jwtUtil.generateToken(savedUser.getUsername());

        return new AuthResponse(token, "Bearer", savedUser.getId(), 
                savedUser.getUsername(), savedUser.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!user.getEnabled()) {
            throw new RuntimeException("账户已被禁用");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        return new AuthResponse(token, "Bearer", user.getId(), 
                user.getUsername(), user.getEmail());
    }
}

