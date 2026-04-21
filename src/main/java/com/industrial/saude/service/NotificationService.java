package com.industrial.saude.service;

import com.industrial.saude.dto.MedicamentoDTO;
import com.industrial.saude.model.Medicamento;
import com.industrial.saude.repository.MedicamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final MedicamentoRepository medicamentoRepository;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private volatile List<EstoqueBaixoItem> ultimaNotificacao = new java.util.ArrayList<>();
    private volatile LocalDateTime ultimoAlerta = null;

    @PostConstruct
    public void init() {
        log.info("[Notification] Servico inicializado");
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void verificarEstoqueBaixoDiario() {
        log.info("[Notification] Verificacao diaria de estoque baixo iniciada");
        verificarEEnviarAlerta();
    }

    public void verificarEEnviarAlerta() {
        List<Medicamento> itensBaixo = medicamentoRepository.findEstoqueBaixo();
        
        List<EstoqueBaixoItem> notificacoes = itensBaixo.stream()
                .map(m -> new EstoqueBaixoItem(
                        m.getId(),
                        m.getNome(),
                        m.getQuantidadeEstoque(),
                        m.getQuantidadeMinima(),
                        m.getUnidade()))
                .collect(Collectors.toList());

        ultimaNotificacao = notificacoes;
        ultimoAlerta = LocalDateTime.now();

        if (!notificacoes.isEmpty()) {
            String msg = String.format("ALERTA: %d item(s) com estoque baixo!", notificacoes.size());
            log.info("[Notification] {}", msg);
            notificarClientes(notificacoes);
        }
    }

    public List<EstoqueBaixoItem> getItensEstoqueBaixo() {
        return medicamentoRepository.findEstoqueBaixo().stream()
                .map(m -> new EstoqueBaixoItem(
                        m.getId(),
                        m.getNome(),
                        m.getQuantidadeEstoque(),
                        m.getQuantidadeMinima(),
                        m.getUnidade()))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStatus() {
        List<EstoqueBaixoItem> itens = getItensEstoqueBaixo();
        boolean temAlerta = !itens.isEmpty();
        
        return Map.of(
                "temAlerta", temAlerta,
                "quantidade", itens.size(),
                "ultimoAlerta", ultimoAlerta != null ? ultimoAlerta.toString() : null,
                "itens", itens
        );
    }

    public void adicionarOuvinte(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECT")
                    .data("Conectado ao servico de notificacoes"));
            
            List<EstoqueBaixoItem> itens = getItensEstoqueBaixo();
            if (!itens.isEmpty()) {
                emitter.send(SseEmitter.event()
                        .name("ESTOQUE_BAIXO")
                        .data(itens));
            }
        } catch (Exception e) {
            log.error("[Notification] Erro ao enviar dados iniciais: {}", e.getMessage());
        }
    }

    private void notificarClientes(List<EstoqueBaixoItem> itens) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ESTOQUE_BAIXO")
                        .data(itens));
            } catch (Exception e) {
                log.error("[Notification] Erro ao notificar cliente: {}", e.getMessage());
                emitters.remove(emitter);
            }
        }
    }

    public static class EstoqueBaixoItem {
        private Long id;
        private String nome;
        private Integer quantidadeAtual;
        private Integer quantidadeMinima;
        private String unidade;

        public EstoqueBaixoItem() {}

        public EstoqueBaixoItem(Long id, String nome, Integer quantidadeAtual, Integer quantidadeMinima, String unidade) {
            this.id = id;
            this.nome = nome;
            this.quantidadeAtual = quantidadeAtual;
            this.quantidadeMinima = quantidadeMinima;
            this.unidade = unidade;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public Integer getQuantidadeAtual() { return quantidadeAtual; }
        public void setQuantidadeAtual(Integer quantidadeAtual) { this.quantidadeAtual = quantidadeAtual; }
        public Integer getQuantidadeMinima() { return quantidadeMinima; }
        public void setQuantidadeMinima(Integer quantidadeMinima) { this.quantidadeMinima = quantidadeMinima; }
        public String getUnidade() { return unidade; }
        public void setUnidade(String unidade) { this.unidade = unidade; }
    }
}