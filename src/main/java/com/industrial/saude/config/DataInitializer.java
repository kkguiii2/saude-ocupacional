package com.industrial.saude.config;

import com.industrial.saude.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializa dados padrão ao subir a aplicação em qualquer ambiente.
 * A criação do admin é idempotente: só ocorre se o usuário ainda não existir no banco.
 * Em produção, a senha é lida da variável de ambiente ADMIN_PASSWORD.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AuthService authService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Inicializando dados do sistema...");
        try {
            authService.criarUsuarioAdmin();
            log.info("Dados inicializados com sucesso");
        } catch (Exception e) {
            log.error("Erro ao inicializar dados: {}", e.getMessage());
        }
    }
}