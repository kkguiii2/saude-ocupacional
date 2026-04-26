package com.industrial.saude.repository;

import com.industrial.saude.model.Medicamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test") // Ensures it uses application-test.properties or H2 by default
class MedicamentoRepositoryTest {

    @Autowired
    private MedicamentoRepository repository;

    @Test
    void shouldSaveAndFindMedicamento() {
        Medicamento medicamento = new Medicamento();
        medicamento.setNome("Amoxicilina");
        medicamento.setQuantidadeEstoque(100);
        medicamento.setQuantidadeMinima(20);

        Medicamento saved = repository.save(medicamento);
        assertNotNull(saved.getId());

        Optional<Medicamento> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Amoxicilina", found.get().getNome());
    }

    @Test
    void shouldFindEstoqueBaixo() {
        Medicamento baixo = new Medicamento();
        baixo.setNome("Dipirona");
        baixo.setQuantidadeEstoque(10);
        baixo.setQuantidadeMinima(20);
        repository.save(baixo);

        Medicamento ok = new Medicamento();
        ok.setNome("Ibuprofeno");
        ok.setQuantidadeEstoque(50);
        ok.setQuantidadeMinima(20);
        repository.save(ok);

        List<Medicamento> estoqueBaixo = repository.findEstoqueBaixo();
        assertFalse(estoqueBaixo.isEmpty());
        assertTrue(estoqueBaixo.stream().anyMatch(m -> m.getNome().equals("Dipirona")));
        assertFalse(estoqueBaixo.stream().anyMatch(m -> m.getNome().equals("Ibuprofeno")));
    }
}
