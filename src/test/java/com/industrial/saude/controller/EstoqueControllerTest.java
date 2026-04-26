package com.industrial.saude.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.industrial.saude.dto.MedicamentoDTO;
import com.industrial.saude.security.JwtAuthenticationFilter;
import com.industrial.saude.security.JwtTokenProvider;
import com.industrial.saude.service.EstoqueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EstoqueController.class)
@AutoConfigureMockMvc(addFilters = false) // Desativa filtros JWT complexos para testar apenas o controller
class EstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstoqueService estoqueService;
    
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldFindAll() throws Exception {
        MedicamentoDTO dto = new MedicamentoDTO();
        dto.setId(1L);
        dto.setNome("Paracetamol");

        when(estoqueService.findAll()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/estoque")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Paracetamol"));
    }

    @Test
    @WithMockUser
    void shouldSave() throws Exception {
        MedicamentoDTO input = new MedicamentoDTO();
        input.setNome("Ibuprofeno");
        input.setQuantidadeEstoque(100);

        MedicamentoDTO output = new MedicamentoDTO();
        output.setId(2L);
        output.setNome("Ibuprofeno");
        output.setQuantidadeEstoque(100);

        when(estoqueService.save(org.mockito.ArgumentMatchers.any(MedicamentoDTO.class))).thenReturn(output);

        mockMvc.perform(post("/api/estoque")
                .with(csrf()) // Necessário se CSRF estiver ativo
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.nome").value("Ibuprofeno"));
    }
}
