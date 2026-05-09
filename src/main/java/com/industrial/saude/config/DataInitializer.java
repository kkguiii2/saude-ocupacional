package com.industrial.saude.config;

import com.industrial.saude.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Inicializa dados padrão para ambiente de desenvolvimento.
 * NÃO executa em produção (perfil "prod").
 * Em prod, crie o admin manualmente ou via script SQL inicial.
 */
@Slf4j
@Component
@Profile("!prod")
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