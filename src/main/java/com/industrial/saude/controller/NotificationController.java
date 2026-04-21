package com.industrial.saude.controller;

import com.industrial.saude.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping("/stream")
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        service.adicionarOuvinte(emitter);
        return emitter;
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<Map<String, Object>> getEstoqueBaixo() {
        return ResponseEntity.ok(service.getStatus());
    }

    @GetMapping("/verificar")
    public ResponseEntity<List<NotificationService.EstoqueBaixoItem>> verificar() {
        service.verificarEEnviarAlerta();
        return ResponseEntity.ok(service.getItensEstoqueBaixo());
    }
}