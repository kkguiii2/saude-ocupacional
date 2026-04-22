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
        try {
            List<Medicamento> itensBaixo = medicamentoRepository.findEstoqueBaixo();

            List<EstoqueBaixoItem> notificacoes = itensBaixo.stream()
                    .map(EstoqueBaixoItem::fromMedicamento)
                    .collect(Collectors.toList());

            ultimaNotificacao = notificacoes;
            ultimoAlerta = LocalDateTime.now();

            if (!notificacoes.isEmpty()) {
                String msg = String.format("ALERTA: %d item(s) com estoque baixo!", notificacoes.size());
                log.info("[Notification] {}", msg);
                notificarClientes(notificacoes);
            }
        } catch (Exception e) {
            log.error("[Notification] Erro ao verificar estoque: {}", e.getMessage(), e);
        }
    }

    public List<EstoqueBaixoItem> getItensEstoqueBaixo() {
        try {
            return medicamentoRepository.findEstoqueBaixo().stream()
                    .map(EstoqueBaixoItem::fromMedicamento)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[Notification] Erro ao buscar itens estoque baixo: {}", e.getMessage(), e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * CORREÇÃO DO NPE:
     *
     * Map.of() é imutável e lança NullPointerException se QUALQUER chave ou valor
     * for null — incluindo ultimoAlerta (nullable) e campos do Medicamento como
     * unidade e quantidadeMinima (colunas sem NOT NULL no banco).
     *
     * Solução: usar HashMap que aceita valores null, e garantir que EstoqueBaixoItem
     * nunca carregue null nos seus campos primitivos/string.
     */
    public Map<String, Object> getStatus() {
        try {
            List<EstoqueBaixoItem> itens = getItensEstoqueBaixo();
            boolean temAlerta = !itens.isEmpty();

            // HashMap aceita valores null — Map.of() NÃO aceita (lança NPE)
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("temAlerta", temAlerta);
            result.put("quantidade", itens.size());
            // ultimoAlerta pode ser null na primeira execução — usamos string vazia
            result.put("ultimoAlerta", ultimoAlerta != null ? ultimoAlerta.toString() : "");
            result.put("itens", itens);
            return result;
        } catch (Exception e) {
            log.error("[Notification] Erro ao montar status: {}", e.getMessage(), e);
            // Fallback seguro — nunca retorna 500 para o frontend
            Map<String, Object> fallback = new java.util.HashMap<>();
            fallback.put("temAlerta", false);
            fallback.put("quantidade", 0);
            fallback.put("ultimoAlerta", "");
            fallback.put("itens", new java.util.ArrayList<>());
            return fallback;
        }
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
            // Garante que campos string nunca sejam null (evita NPE downstream e Map.of)
            this.nome = nome != null ? nome : "";
            this.unidade = unidade != null ? unidade : "";
            // Garante que campos numéricos nunca sejam null
            this.quantidadeAtual = quantidadeAtual != null ? quantidadeAtual : 0;
            this.quantidadeMinima = quantidadeMinima != null ? quantidadeMinima : 0;
        }

        /**
         * Factory method que sanitiza todos os campos nullable do Medicamento.
         * quantidadeMinima e unidade não têm NOT NULL no banco, então podem chegar null.
         */
        public static EstoqueBaixoItem fromMedicamento(Medicamento m) {
            return new EstoqueBaixoItem(
                    m.getId(),
                    m.getNome(),
                    m.getQuantidadeEstoque(),
                    m.getQuantidadeMinima(),   // nullable no banco
                    m.getUnidade()             // nullable no banco
            );
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