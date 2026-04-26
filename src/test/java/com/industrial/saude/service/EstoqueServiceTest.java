package com.industrial.saude.service;

import com.industrial.saude.dto.MedicamentoDTO;
import com.industrial.saude.model.Medicamento;
import com.industrial.saude.repository.MedicamentoRepository;
import com.industrial.saude.repository.MovimentacaoEstoqueRepository;
import com.industrial.saude.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private MedicamentoRepository repository;
    
    @Mock
    private MovimentacaoEstoqueRepository movimentacaoRepository;
    
    @Mock
    private UsuarioRepository usuarioRepository;
    
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private EstoqueService estoqueService;

    private Medicamento medicamento;

    @BeforeEach
    void setUp() {
        medicamento = new Medicamento();
        medicamento.setId(1L);
        medicamento.setNome("Paracetamol");
        medicamento.setQuantidadeEstoque(50);
        medicamento.setQuantidadeMinima(20);
        medicamento.setUnidade("Comprimido");
    }

    @Test
    void shouldFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(medicamento));

        MedicamentoDTO dto = estoqueService.findById(1L);

        assertNotNull(dto);
        assertEquals("Paracetamol", dto.getNome());
        assertEquals(50, dto.getQuantidadeEstoque());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void shouldSaveNewMedicamento() {
        MedicamentoDTO inputDto = new MedicamentoDTO();
        inputDto.setNome("Ibuprofeno");
        inputDto.setQuantidadeEstoque(100);

        Medicamento savedEntity = new Medicamento();
        savedEntity.setId(2L);
        savedEntity.setNome("Ibuprofeno");
        savedEntity.setQuantidadeEstoque(100);

        when(repository.save(any(Medicamento.class))).thenReturn(savedEntity);

        MedicamentoDTO result = estoqueService.save(inputDto);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Ibuprofeno", result.getNome());
        
        // Verifica se a auditoria foi chamada, pois é um novo medicamento
        verify(auditoriaService, times(1)).registrar(anyString(), anyString(), anyString(), anyString());
    }
}
