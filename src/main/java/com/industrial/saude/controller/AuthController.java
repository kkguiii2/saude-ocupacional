package com.industrial.saude.controller;

import com.industrial.saude.dto.LoginRequest;
import com.industrial.saude.dto.LoginResponse;
import com.industrial.saude.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIP(httpRequest);
        log.info("Login API request - Username: {} - IP: {}", request.getUsername(), ip);

        try {
            LoginResponse response = authService.login(request, ip);
            log.info("Login bem-sucedido - Username: {} - Token gerado", response.getUsername());
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            log.warn("Login bloqueado - Username: {} - Motivo: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Erro no login - Username: {} - Erro: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?loggedout";
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}