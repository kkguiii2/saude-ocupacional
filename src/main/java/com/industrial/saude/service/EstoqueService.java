package com.industrial.saude.service;

import com.industrial.saude.dto.MedicamentoDTO;
import com.industrial.saude.model.Medicamento;
import com.industrial.saude.model.MovimentacaoEstoque;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.repository.MedicamentoRepository;
import com.industrial.saude.repository.MovimentacaoEstoqueRepository;
import com.industrial.saude.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstoqueService {
    
    private final MedicamentoRepository repository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    
    public List<MedicamentoDTO> findAll() {
        return repository.findByAtivo(true).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<MedicamentoDTO> findEstoqueBaixo() {
        return repository.findEstoqueBaixo().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public MedicamentoDTO findById(Long id) {
        return repository.findById(id).map(this::toDTO).orElse(null);
    }
    
    @Transactional
    public MedicamentoDTO save(MedicamentoDTO dto) {
        boolean isNew = dto.getId() == null;
        Medicamento entity = toEntity(dto);
        entity = repository.save(entity);

        if (dto.getId() == null) {
            auditoriaService.registrar("sistema", AuditoriaService.ACAO_CREATE, "ESTOQUE",
                    "Medicamento cadastrado: " + entity.getNome());
        }

        return toDTO(entity);
    }

    @Transactional
    public MedicamentoDTO entrada(Long id, int quantidade, Long usuarioId) {
        Medicamento entity = repository.findById(id).orElse(null);
        if (entity == null) return null;

        entity.setQuantidadeEstoque(entity.getQuantidadeEstoque() + quantidade);
        repository.save(entity);

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setMedicamento(entity);
        mov.setQuantidade(quantidade);
        mov.setTipo(MovimentacaoEstoque.TipoMovimentacao.ENTRADA);
        Usuario responsavel = usuarioRepository.findById(usuarioId).orElse(null);
        mov.setResponsavel(responsavel);
        movimentacaoRepository.save(mov);

        String username = responsavel != null ? responsavel.getUsername() : "sistema";
        auditoriaService.registrar(username, AuditoriaService.ACAO_ENTRADA, "ESTOQUE",
                "Entrada de estoque: " + entity.getNome() + " (" + quantidade + " " + entity.getUnidade() + ")");

        return toDTO(entity);
    }

    @Transactional
    public MedicamentoDTO saida(Long id, int quantidade, Long usuarioId) {
        Medicamento entity = repository.findById(id).orElse(null);
        if (entity == null) return null;

        entity.setQuantidadeEstoque(Math.max(0, entity.getQuantidadeEstoque() - quantidade));
        repository.save(entity);

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setMedicamento(entity);
        mov.setQuantidade(quantidade);
        mov.setTipo(MovimentacaoEstoque.TipoMovimentacao.SAIDA);
        Usuario responsavel = usuarioRepository.findById(usuarioId).orElse(null);
        mov.setResponsavel(responsavel);
        movimentacaoRepository.save(mov);

        String username = responsavel != null ? responsavel.getUsername() : "sistema";
        auditoriaService.registrar(username, AuditoriaService.ACAO_SAIDA, "ESTOQUE",
                "Saída de estoque: " + entity.getNome() + " (" + quantidade + " " + entity.getUnidade() + ")");

        return toDTO(entity);
    }
    
    public long countEstoqueBaixo() {
        return repository.countEstoqueBaixo();
    }
    
    private MedicamentoDTO toDTO(Medicamento entity) {
        MedicamentoDTO dto = new MedicamentoDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setPrincipioAtivo(entity.getPrincipioAtivo());
        dto.setQuantidadeEstoque(entity.getQuantidadeEstoque());
        dto.setQuantidadeMinima(entity.getQuantidadeMinima());
        dto.setUnidade(entity.getUnidade());
        dto.setCategoria(entity.getCategoria());
        if (entity.getDataValidade() != null) {
            dto.setDataValidade(entity.getDataValidade().toString());
        }
        return dto;
    }
    
    private Medicamento toEntity(MedicamentoDTO dto) {
        Medicamento entity = new Medicamento();
        if (dto.getId() != null) entity.setId(dto.getId());
        entity.setNome(dto.getNome());
        entity.setPrincipioAtivo(dto.getPrincipioAtivo());
        entity.setQuantidadeEstoque(dto.getQuantidadeEstoque());
        entity.setQuantidadeMinima(dto.getQuantidadeMinima());
        entity.setUnidade(dto.getUnidade());
        entity.setCategoria(dto.getCategoria());
        return entity;
    }
}