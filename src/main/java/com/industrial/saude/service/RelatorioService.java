package com.industrial.saude.service;

import com.industrial.saude.model.*;
import com.industrial.saude.repository.*;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final AtendimentoRepository atendimentoRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AcidenteTrabalhoRepository acidenteTrabalhoRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] gerarPdfAtendimento(Long atendimentoId) throws Exception {
        Atendimento atendimento = atendimentoRepository.findById(atendimentoId)
                .orElseThrow(() -> new RuntimeException("Atendimento não encontrado"));

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        addHeader(document, "RELATÓRIO DE ATENDIMENTO", "Sistema de Saúde Ocupacional");

        addBlankLines(document, 1);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 30, 70 });

        addRow(table, "Nº do Atendimento", String.valueOf(atendimento.getId()));
        addRow(table, "Data/Hora", atendimento.getDataHora().format(DATE_FORMATTER));
        addRow(table, "Tipo", formatTipoAtendimento(atendimento.getTipo()));
        addRow(table, "Gravidade", atendimento.getGravidade().name());
        addRow(table, "Emergência", atendimento.isEmergencia() ? "SIM" : "NÃO");

        document.add(table);

        addBlankLines(document, 1);

        addSectionTitle(document, "DADOS DO COLABORADOR");

        PdfPTable colabTable = new PdfPTable(2);
        colabTable.setWidthPercentage(100);
        colabTable.setWidths(new float[] { 30, 70 });

        Colaborador colab = atendimento.getColaborador();
        addRow(colabTable, "Nome", colab.getNomeCompleto());
        addRow(colabTable, "Matrícula", colab.getMatricula());
        addRow(colabTable, "Setor", colab.getSetor().name());
        addRow(colabTable, "Cargo", colab.getCargo());
        addRow(colabTable, "Tipo de Risco", colab.getTipoRisco().name());

        document.add(colabTable);

        addBlankLines(document, 1);

        addSectionTitle(document, "DESCRIÇÃO DO ATENDIMENTO");

        if (atendimento.getSintomas() != null && !atendimento.getSintomas().isEmpty()) {
            addFieldValue(document, "Sintomas/Descrição:", atendimento.getSintomas());
        }

        if (atendimento.getConduta() != null && !atendimento.getConduta().isEmpty()) {
            addFieldValue(document, "Conduta/Tratamento:", atendimento.getConduta());
        }

        if (atendimento.getEncaminhamento() != null) {
            addFieldValue(document, "Encaminhamento:", atendimento.getEncaminhamento().name().replace("_", " "));
        }

        addBlankLines(document, 1);

        addSectionTitle(document, "PROFISSIONAL RESPONSÁVEL");

        PdfPTable profTable = new PdfPTable(2);
        profTable.setWidthPercentage(100);
        profTable.setWidths(new float[] { 30, 70 });

        Usuario atendente = atendimento.getAtendente();
        addRow(profTable, "Nome do Profissional", atendente.getNome());
        addRow(profTable, "Perfil", atendente.getPerfil().name().replace("_", " "));

        document.add(profTable);

        addBlankLines(document, 2);

        addFooter(document);

        document.close();
        return baos.toByteArray();
    }

    public byte[] gerarPdfHistoricoColaborador(Long colaboradorId) throws Exception {
        Colaborador colab = colaboradorRepository.findById(colaboradorId)
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado"));

        List<Atendimento> atendimentos = atendimentoRepository.findByColaboradorId(colaboradorId);
        List<Agendamento> agendamentos = agendamentoRepository.findByColaboradorId(colaboradorId);
        List<AcidenteTrabalho> accidentes = acidenteTrabalhoRepository.findByColaboradorId(colaboradorId);

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        addHeader(document, "HISTÓRICO DO COLABORADOR", "Sistema de Saúde Ocupacional");

        addBlankLines(document, 1);

        addSectionTitle(document, "DADOS PESSOAIS");

        PdfPTable dadosTable = new PdfPTable(2);
        dadosTable.setWidthPercentage(100);
        dadosTable.setWidths(new float[] { 30, 70 });

        addRow(dadosTable, "Nome", colab.getNomeCompleto());
        addRow(dadosTable, "Matrícula", colab.getMatricula());
        addRow(dadosTable, "Setor", colab.getSetor().name());
        addRow(dadosTable, "Cargo", colab.getCargo());
        addRow(dadosTable, "Data de Admissão",
                colab.getDataAdmissao() != null ? colab.getDataAdmissao().format(DATE_ONLY) : "N/A");
        addRow(dadosTable, "Status", colab.getStatusFuncionario().name());
        addRow(dadosTable, "Tipo de Risco", colab.getTipoRisco().name());
        addRow(dadosTable, "EPI's Obrigatórios",
                colab.getEpisObrigatorios() != null ? colab.getEpisObrigatorios() : "Nenhum");
        addRow(dadosTable, "Contato de Emergência",
                colab.getContatoEmergencia() != null ? colab.getContatoEmergencia() : "N/A");

        document.add(dadosTable);

        addBlankLines(document, 1);

        addSectionTitle(document, "HISTÓRICO DE ATENDIMENTOS (" + atendimentos.size() + ")");

        if (atendimentos.isEmpty()) {
            addPlainText(document, "Nenhum atendimento registrado.");
        } else {
            for (Atendimento a : atendimentos) {
                PdfPTable atTable = new PdfPTable(2);
                atTable.setWidthPercentage(100);
                atTable.setWidths(new float[] { 30, 70 });

                addRow(atTable, "Data", a.getDataHora().format(DATE_FORMATTER));
                addRow(atTable, "Tipo", formatTipoAtendimento(a.getTipo()));
                addRow(atTable, "Gravidade", a.getGravidade().name());
                addRow(atTable, "Emergência", a.isEmergencia() ? "SIM" : "NÃO");
                if (a.getSintomas() != null) {
                    addRow(atTable, "Descrição", a.getSintomas());
                }
                if (a.getConduta() != null) {
                    addRow(atTable, "Conduta", a.getConduta());
                }
                if (a.getEncaminhamento() != null) {
                    addRow(atTable, "Encaminhamento", a.getEncaminhamento().name().replace("_", " "));
                }

                document.add(atTable);
                addBlankLines(document, 1);
            }
        }

        addBlankLines(document, 1);

        addSectionTitle(document, "HISTÓRICO DE ACIDENTES DE TRABALHO (" + accidentes.size() + ")");

        if (accidentes.isEmpty()) {
            addPlainText(document, "Nenhum acidente registrado.");
        } else {
            for (AcidenteTrabalho ac : accidentes) {
                PdfPTable acTable = new PdfPTable(2);
                acTable.setWidthPercentage(100);
                acTable.setWidths(new float[] { 30, 70 });

                addRow(acTable, "Data", ac.getDataHora().format(DATE_FORMATTER));
                addRow(acTable, "Tipo", ac.getTipo().name());
                addRow(acTable, "Local", ac.getLocalFabrica());
                if (ac.getDescricao() != null) {
                    addRow(acTable, "Descrição", ac.getDescricao());
                }
                if (ac.getCausa() != null) {
                    addRow(acTable, "Causa", ac.getCausa());
                }
                addRow(acTable, "CAT Emitida", ac.isCatEmitida() ? "SIM - " + ac.getNumeroCat() : "NÃO");

                document.add(acTable);
                addBlankLines(document, 1);
            }
        }

        addBlankLines(document, 1);

        addSectionTitle(document, "AGENDAMENTOS REALIZADOS (" + agendamentos.size() + ")");

        if (agendamentos.isEmpty()) {
            addPlainText(document, "Nenhum agendamento registrado.");
        } else {
            for (Agendamento ag : agendamentos) {
                PdfPTable agTable = new PdfPTable(2);
                agTable.setWidthPercentage(100);
                agTable.setWidths(new float[] { 30, 70 });

                addRow(agTable, "Data", ag.getDataHora().format(DATE_FORMATTER));
                addRow(agTable, "Tipo", ag.getTipo().name());
                addRow(agTable, "Status", ag.getStatus().name());
                if (ag.getObservacoes() != null) {
                    addRow(agTable, "Observações", ag.getObservacoes());
                }

                document.add(agTable);
                addBlankLines(document, 1);
            }
        }

        addBlankLines(document, 2);

        addFooter(document);

        document.close();
        return baos.toByteArray();
    }

    public byte[] gerarExcelColaboradores() {
        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Colaboradores");
        String[] headers = { "ID", "Nome", "Matrícula", "Setor", "Cargo", "Tipo Risco", "Status", "Data Admissão", "EPI's" };
        ExcelStyles s = createExcelHeader(workbook, sheet, headers);

        int rowNum = 1;
        for (Colaborador colab : colaboradores) {
            Row row = sheet.createRow(rowNum);
            styledNumericCell(row, 0, colab.getId(), s, rowNum);
            styledCell(row, 1, colab.getNomeCompleto(), s, rowNum);
            styledCell(row, 2, colab.getMatricula(), s, rowNum);
            styledCell(row, 3, colab.getSetor() != null ? colab.getSetor().name() : "", s, rowNum);
            styledCell(row, 4, colab.getCargo(), s, rowNum);
            styledCell(row, 5, colab.getTipoRisco() != null ? colab.getTipoRisco().name() : "", s, rowNum);
            styledCell(row, 6, colab.getStatusFuncionario() != null ? colab.getStatusFuncionario().name() : "", s, rowNum);
            styledCell(row, 7, colab.getDataAdmissao() != null ? colab.getDataAdmissao().toString() : "", s, rowNum);
            styledCell(row, 8, colab.getEpisObrigatorios(), s, rowNum);
            rowNum++;
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    public byte[] gerarExcelAtendimentos() {
        List<Atendimento> atendimentos = atendimentoRepository.findAll().stream().filter(Atendimento::isAtivo).toList();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Atendimentos");
        String[] headers = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Gravidade", "Emergência", "Sintomas", "Conduta", "Encaminhamento", "Profissional" };
        ExcelStyles s = createExcelHeader(workbook, sheet, headers);

        int rowNum = 1;
        for (Atendimento at : atendimentos) {
            Row row = sheet.createRow(rowNum);
            styledNumericCell(row, 0, at.getId(), s, rowNum);
            styledCell(row, 1, at.getDataHora().format(DATE_FORMATTER), s, rowNum);
            styledCell(row, 2, at.getColaborador().getNomeCompleto(), s, rowNum);
            styledCell(row, 3, at.getColaborador().getMatricula(), s, rowNum);
            styledCell(row, 4, at.getColaborador().getSetor() != null ? at.getColaborador().getSetor().name() : "", s, rowNum);
            styledCell(row, 5, formatTipoAtendimento(at.getTipo()), s, rowNum);
            styledCell(row, 6, at.getGravidade().name(), s, rowNum);
            styledCell(row, 7, at.isEmergencia() ? "SIM" : "NÃO", s, rowNum);
            styledCell(row, 8, at.getSintomas(), s, rowNum);
            styledCell(row, 9, at.getConduta(), s, rowNum);
            styledCell(row, 10, at.getEncaminhamento() != null ? at.getEncaminhamento().name() : "", s, rowNum);
            styledCell(row, 11, at.getAtendente() != null ? at.getAtendente().getNome() : "", s, rowNum);
            rowNum++;
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    public byte[] gerarExcelAgendamentos() {
        List<Agendamento> agendamentos = agendamentoRepository.findAll();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Agendamentos");
        String[] headers = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Status", "Observações", "Agendado Por" };
        ExcelStyles s = createExcelHeader(workbook, sheet, headers);
        int rowNum = 1;
        for (Agendamento ag : agendamentos) {
            Row row = sheet.createRow(rowNum);
            styledNumericCell(row, 0, ag.getId(), s, rowNum);
            styledCell(row, 1, ag.getDataHora().format(DATE_FORMATTER), s, rowNum);
            styledCell(row, 2, ag.getColaborador().getNomeCompleto(), s, rowNum);
            styledCell(row, 3, ag.getColaborador().getMatricula(), s, rowNum);
            styledCell(row, 4, ag.getColaborador().getSetor() != null ? ag.getColaborador().getSetor().name() : "", s, rowNum);
            styledCell(row, 5, ag.getTipo().name(), s, rowNum);
            styledCell(row, 6, ag.getStatus().name(), s, rowNum);
            styledCell(row, 7, ag.getObservacoes(), s, rowNum);
            styledCell(row, 8, ag.getAgendadoPor() != null ? ag.getAgendadoPor().getNome() : "", s, rowNum);
            rowNum++;
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    public byte[] gerarExcelEstoque() {
        List<Medicamento> medicamentos = medicamentoRepository.findAll();
        List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAll();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sm = workbook.createSheet("Medicamentos");
        Sheet sv = workbook.createSheet("Movimentações");
        String[] h1 = { "ID", "Nome", "Princípio Ativo", "Categoria", "Quantidade", "Mínimo", "Unidade", "Validade", "Lote" };
        ExcelStyles s1 = createExcelHeader(workbook, sm, h1);
        int rn = 1;
        for (Medicamento med : medicamentos) {
            Row row = sm.createRow(rn);
            styledNumericCell(row, 0, med.getId(), s1, rn);
            styledCell(row, 1, med.getNome(), s1, rn);
            styledCell(row, 2, med.getPrincipioAtivo(), s1, rn);
            styledCell(row, 3, med.getCategoria() != null ? med.getCategoria().name() : "", s1, rn);
            styledNumericCell(row, 4, med.getQuantidadeEstoque(), s1, rn);
            styledNumericCell(row, 5, med.getQuantidadeMinima() != null ? med.getQuantidadeMinima() : 0, s1, rn);
            styledCell(row, 6, med.getUnidade(), s1, rn);
            styledCell(row, 7, med.getDataValidade() != null ? med.getDataValidade().format(DATE_FORMATTER) : "", s1, rn);
            styledCell(row, 8, med.getLote(), s1, rn);
            rn++;
        }
        for (int i = 0; i < h1.length; i++) sm.autoSizeColumn(i);
        String[] h2 = { "ID", "Data", "Tipo", "Medicamento", "Quantidade", "Descrição", "Usuário" };
        ExcelStyles s2 = createExcelHeader(workbook, sv, h2);
        rn = 1;
        for (MovimentacaoEstoque mov : movimentacoes) {
            Row row = sv.createRow(rn);
            styledNumericCell(row, 0, mov.getId(), s2, rn);
            styledCell(row, 1, mov.getDataHora().format(DATE_FORMATTER), s2, rn);
            styledCell(row, 2, mov.getTipo().name(), s2, rn);
            styledCell(row, 3, mov.getMedicamento().getNome(), s2, rn);
            styledNumericCell(row, 4, mov.getQuantidade(), s2, rn);
            styledCell(row, 5, mov.getMotivo(), s2, rn);
            styledCell(row, 6, mov.getResponsavel() != null ? mov.getResponsavel().getNome() : "", s2, rn);
            rn++;
        }
        for (int i = 0; i < h2.length; i++) sv.autoSizeColumn(i);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    public byte[] gerarExcelAcidentes() {
        List<AcidenteTrabalho> acidentes = acidenteTrabalhoRepository.findAll();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Acidentes de Trabalho");
        String[] headers = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Local", "Descrição", "Causa", "CAT Emitida", "Nº CAT", "Registrado Por" };
        ExcelStyles s = createExcelHeader(workbook, sheet, headers);
        int rowNum = 1;
        for (AcidenteTrabalho ac : acidentes) {
            Row row = sheet.createRow(rowNum);
            styledNumericCell(row, 0, ac.getId(), s, rowNum);
            styledCell(row, 1, ac.getDataHora().format(DATE_FORMATTER), s, rowNum);
            styledCell(row, 2, ac.getColaborador().getNomeCompleto(), s, rowNum);
            styledCell(row, 3, ac.getColaborador().getMatricula(), s, rowNum);
            styledCell(row, 4, ac.getColaborador().getSetor() != null ? ac.getColaborador().getSetor().name() : "", s, rowNum);
            styledCell(row, 5, ac.getTipo().name(), s, rowNum);
            styledCell(row, 6, ac.getLocalFabrica(), s, rowNum);
            styledCell(row, 7, ac.getDescricao(), s, rowNum);
            styledCell(row, 8, ac.getCausa(), s, rowNum);
            styledCell(row, 9, ac.isCatEmitida() ? "SIM" : "NÃO", s, rowNum);
            styledCell(row, 10, ac.getNumeroCat(), s, rowNum);
            styledCell(row, 11, ac.getRegistradoPor() != null ? ac.getRegistradoPor().getNome() : "", s, rowNum);
            rowNum++;
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel de acidentes: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    /**
     * Gera um Excel completo com múltiplas abas de acordo com os módulos
     * selecionados.
     * 
     * @param modulos lista de módulos desejados: colaboradores, atendimentos,
     *                agendamentos, acidentes, medicamentos, movimentacoes
     */
    public byte[] gerarExcelCompleto(List<String> modulos) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        if (modulos.contains("colaboradores")) {
            List<Colaborador> colaboradores = colaboradorRepository.findAll();
            Sheet sheet = workbook.createSheet("Colaboradores");
            String[] h = { "ID", "Nome", "Matrícula", "Setor", "Cargo", "Tipo Risco", "Status", "Data Admissão", "EPI's" };
            ExcelStyles s = createExcelHeader(workbook, sheet, h);
            int rn = 1;
            for (Colaborador c : colaboradores) {
                Row row = sheet.createRow(rn);
                styledNumericCell(row, 0, c.getId(), s, rn);
                styledCell(row, 1, c.getNomeCompleto(), s, rn);
                styledCell(row, 2, c.getMatricula(), s, rn);
                styledCell(row, 3, c.getSetor() != null ? c.getSetor().name() : "", s, rn);
                styledCell(row, 4, c.getCargo(), s, rn);
                styledCell(row, 5, c.getTipoRisco() != null ? c.getTipoRisco().name() : "", s, rn);
                styledCell(row, 6, c.getStatusFuncionario() != null ? c.getStatusFuncionario().name() : "", s, rn);
                styledCell(row, 7, c.getDataAdmissao() != null ? c.getDataAdmissao().toString() : "", s, rn);
                styledCell(row, 8, c.getEpisObrigatorios(), s, rn);
                rn++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
        }

        if (modulos.contains("atendimentos")) {
            List<Atendimento> atendimentos = atendimentoRepository.findAll().stream().filter(Atendimento::isAtivo).toList();
            Sheet sheet = workbook.createSheet("Atendimentos");
            String[] h = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Gravidade", "Emergência", "Sintomas", "Conduta", "Encaminhamento", "Profissional" };
            ExcelStyles s = createExcelHeader(workbook, sheet, h);
            int rn = 1;
            for (Atendimento at : atendimentos) {
                Row row = sheet.createRow(rn);
                styledNumericCell(row, 0, at.getId(), s, rn);
                styledCell(row, 1, at.getDataHora().format(DATE_FORMATTER), s, rn);
                styledCell(row, 2, at.getColaborador().getNomeCompleto(), s, rn);
                styledCell(row, 3, at.getColaborador().getMatricula(), s, rn);
                styledCell(row, 4, at.getColaborador().getSetor() != null ? at.getColaborador().getSetor().name() : "", s, rn);
                styledCell(row, 5, formatTipoAtendimento(at.getTipo()), s, rn);
                styledCell(row, 6, at.getGravidade().name(), s, rn);
                styledCell(row, 7, at.isEmergencia() ? "SIM" : "NÃO", s, rn);
                styledCell(row, 8, at.getSintomas(), s, rn);
                styledCell(row, 9, at.getConduta(), s, rn);
                styledCell(row, 10, at.getEncaminhamento() != null ? at.getEncaminhamento().name() : "", s, rn);
                styledCell(row, 11, at.getAtendente() != null ? at.getAtendente().getNome() : "", s, rn);
                rn++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
        }

        if (modulos.contains("agendamentos")) {
            List<Agendamento> agendamentos = agendamentoRepository.findAll();
            Sheet sheet = workbook.createSheet("Agendamentos");
            String[] h = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Status", "Observações", "Agendado Por" };
            ExcelStyles s = createExcelHeader(workbook, sheet, h);
            int rn = 1;
            for (Agendamento ag : agendamentos) {
                Row row = sheet.createRow(rn);
                styledNumericCell(row, 0, ag.getId(), s, rn);
                styledCell(row, 1, ag.getDataHora().format(DATE_FORMATTER), s, rn);
                styledCell(row, 2, ag.getColaborador().getNomeCompleto(), s, rn);
                styledCell(row, 3, ag.getColaborador().getMatricula(), s, rn);
                styledCell(row, 4, ag.getColaborador().getSetor() != null ? ag.getColaborador().getSetor().name() : "", s, rn);
                styledCell(row, 5, ag.getTipo().name(), s, rn);
                styledCell(row, 6, ag.getStatus().name(), s, rn);
                styledCell(row, 7, ag.getObservacoes(), s, rn);
                styledCell(row, 8, ag.getAgendadoPor() != null ? ag.getAgendadoPor().getNome() : "", s, rn);
                rn++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
        }

        if (modulos.contains("acidentes")) {
            List<AcidenteTrabalho> acidentes = acidenteTrabalhoRepository.findAll();
            Sheet sheet = workbook.createSheet("Acidentes de Trabalho");
            String[] h = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Local", "Descrição", "Causa", "CAT Emitida", "Nº CAT", "Registrado Por" };
            ExcelStyles s = createExcelHeader(workbook, sheet, h);
            int rn = 1;
            for (AcidenteTrabalho ac : acidentes) {
                Row row = sheet.createRow(rn);
                styledNumericCell(row, 0, ac.getId(), s, rn);
                styledCell(row, 1, ac.getDataHora().format(DATE_FORMATTER), s, rn);
                styledCell(row, 2, ac.getColaborador().getNomeCompleto(), s, rn);
                styledCell(row, 3, ac.getColaborador().getMatricula(), s, rn);
                styledCell(row, 4, ac.getColaborador().getSetor() != null ? ac.getColaborador().getSetor().name() : "", s, rn);
                styledCell(row, 5, ac.getTipo().name(), s, rn);
                styledCell(row, 6, ac.getLocalFabrica(), s, rn);
                styledCell(row, 7, ac.getDescricao(), s, rn);
                styledCell(row, 8, ac.getCausa(), s, rn);
                styledCell(row, 9, ac.isCatEmitida() ? "SIM" : "NÃO", s, rn);
                styledCell(row, 10, ac.getNumeroCat(), s, rn);
                styledCell(row, 11, ac.getRegistradoPor() != null ? ac.getRegistradoPor().getNome() : "", s, rn);
                rn++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
        }

        if (modulos.contains("medicamentos")) {
            List<Medicamento> medicamentos = medicamentoRepository.findAll();
            Sheet sheet = workbook.createSheet("Medicamentos");
            String[] h = { "ID", "Nome", "Princípio Ativo", "Categoria", "Quantidade", "Mínimo", "Unidade", "Validade", "Lote" };
            ExcelStyles s = createExcelHeader(workbook, sheet, h);
            int rn = 1;
            for (Medicamento med : medicamentos) {
                Row row = sheet.createRow(rn);
                styledNumericCell(row, 0, med.getId(), s, rn);
                styledCell(row, 1, med.getNome(), s, rn);
                styledCell(row, 2, med.getPrincipioAtivo(), s, rn);
                styledCell(row, 3, med.getCategoria() != null ? med.getCategoria().name() : "", s, rn);
                styledNumericCell(row, 4, med.getQuantidadeEstoque(), s, rn);
                styledNumericCell(row, 5, med.getQuantidadeMinima() != null ? med.getQuantidadeMinima() : 0, s, rn);
                styledCell(row, 6, med.getUnidade(), s, rn);
                styledCell(row, 7, med.getDataValidade() != null ? med.getDataValidade().format(DATE_FORMATTER) : "", s, rn);
                styledCell(row, 8, med.getLote(), s, rn);
                rn++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
        }

        if (modulos.contains("movimentacoes")) {
            List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAll();
            Sheet sheet = workbook.createSheet("Movimentações Estoque");
            String[] h = { "ID", "Data", "Tipo", "Medicamento", "Quantidade", "Motivo", "Responsável" };
            ExcelStyles s = createExcelHeader(workbook, sheet, h);
            int rn = 1;
            for (MovimentacaoEstoque mov : movimentacoes) {
                Row row = sheet.createRow(rn);
                styledNumericCell(row, 0, mov.getId(), s, rn);
                styledCell(row, 1, mov.getDataHora().format(DATE_FORMATTER), s, rn);
                styledCell(row, 2, mov.getTipo().name(), s, rn);
                styledCell(row, 3, mov.getMedicamento().getNome(), s, rn);
                styledNumericCell(row, 4, mov.getQuantidade(), s, rn);
                styledCell(row, 5, mov.getMotivo(), s, rn);
                styledCell(row, 6, mov.getResponsavel() != null ? mov.getResponsavel().getNome() : "", s, rn);
                rn++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel completo: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    public byte[] gerarExcelNR7() {
        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório PCMSO (NR-7 e eSocial)");
        String[] headers = {
            "ID", "Nome do Colaborador", "Matrícula", "PIS/PASEP", "Setor", "Cargo",
            "Data Admissão", "Risco Ocupacional", "Último Exame (ASO)", "Próximo Exame",
            "Restrições", "Observações Médicas"
        };
        ExcelStyles s = createExcelHeader(workbook, sheet, headers);

        int rowNum = 1;
        for (Colaborador colab : colaboradores) {
            Row row = sheet.createRow(rowNum);
            styledNumericCell(row, 0, colab.getId(), s, rowNum);
            styledCell(row, 1, colab.getNomeCompleto(), s, rowNum);
            styledCell(row, 2, colab.getMatricula(), s, rowNum);
            styledCell(row, 3, colab.getPisPasep() != null ? colab.getPisPasep() : "", s, rowNum);
            styledCell(row, 4, colab.getSetor() != null ? colab.getSetor().name() : "", s, rowNum);
            styledCell(row, 5, colab.getCargo(), s, rowNum);
            styledCell(row, 6, colab.getDataAdmissao() != null ? colab.getDataAdmissao().toString() : "", s, rowNum);
            styledCell(row, 7, colab.getTipoRisco() != null ? colab.getTipoRisco().name() : "", s, rowNum);
            
            ProntuarioOcupacional p = colab.getProntuario();
            if (p != null) {
                styledCell(row, 8, p.getUltimoExame() != null ? p.getUltimoExame().format(DATE_FORMATTER) : "", s, rowNum);
                styledCell(row, 9, p.getProximoExame() != null ? p.getProximoExame().format(DATE_FORMATTER) : "", s, rowNum);
                styledCell(row, 10, p.getRestricoesTrabalho() != null ? p.getRestricoesTrabalho() : "", s, rowNum);
                String obs = "";
                if (p.getAlergias() != null) obs += "Alergias: " + p.getAlergias() + " ";
                if (p.getMedicacoesUso() != null) obs += "Med: " + p.getMedicacoesUso();
                styledCell(row, 11, obs.trim(), s, rowNum);
            } else {
                styledCell(row, 8, "", s, rowNum);
                styledCell(row, 9, "", s, rowNum);
                styledCell(row, 10, "", s, rowNum);
                styledCell(row, 11, "Sem Prontuário", s, rowNum);
            }
            rowNum++;
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel NR-7: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    // ─── Inner class that holds the three cell styles for one workbook ────────
    private static class ExcelStyles {
        final XSSFCellStyle header;
        final XSSFCellStyle rowEven;
        final XSSFCellStyle rowOdd;
        final XSSFCellStyle rowEvenRight;
        final XSSFCellStyle rowOddRight;

        ExcelStyles(XSSFCellStyle header, XSSFCellStyle rowEven, XSSFCellStyle rowOdd,
                XSSFCellStyle rowEvenRight, XSSFCellStyle rowOddRight) {
            this.header = header;
            this.rowEven = rowEven;
            this.rowOdd = rowOdd;
            this.rowEvenRight = rowEvenRight;
            this.rowOddRight = rowOddRight;
        }
    }

    /** Builds all professional styles once per workbook (avoids POI style-limit). */
    private ExcelStyles buildStyles(XSSFWorkbook wb) {
        // ── Header: bold, dark text, light-gray background ──────────────────
        XSSFCellStyle hStyle = wb.createCellStyle();
        XSSFFont hFont = wb.createFont();
        hFont.setBold(true);
        hFont.setFontHeightInPoints((short) 10);
        hFont.setColor(IndexedColors.BLACK1.getIndex());
        hStyle.setFont(hFont);
        // Light gray (hex #D9D9D9)
        hStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)0xD9, (byte)0xD9, (byte)0xD9}, null));
        hStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        hStyle.setAlignment(HorizontalAlignment.CENTER);
        applyBorder(hStyle);

        // ── Even row: white background ────────────────────────────────────
        XSSFCellStyle eStyle = wb.createCellStyle();
        XSSFFont dataFont = wb.createFont();
        dataFont.setFontHeightInPoints((short) 10);
        eStyle.setFont(dataFont);
        eStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF}, null));
        eStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        eStyle.setAlignment(HorizontalAlignment.LEFT);
        eStyle.setWrapText(false);
        applyBorder(eStyle);

        // ── Odd row: very light blue-gray ────────────────────────────────
        XSSFCellStyle oStyle = wb.createCellStyle();
        oStyle.setFont(dataFont);
        oStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)0xF2, (byte)0xF2, (byte)0xF2}, null));
        oStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        oStyle.setAlignment(HorizontalAlignment.LEFT);
        oStyle.setWrapText(false);
        applyBorder(oStyle);

        // ── Right-aligned variants (for numeric columns) ─────────────────
        XSSFCellStyle eRight = wb.createCellStyle();
        eRight.cloneStyleFrom(eStyle);
        eRight.setAlignment(HorizontalAlignment.RIGHT);

        XSSFCellStyle oRight = wb.createCellStyle();
        oRight.cloneStyleFrom(oStyle);
        oRight.setAlignment(HorizontalAlignment.RIGHT);

        return new ExcelStyles(hStyle, eStyle, oStyle, eRight, oRight);
    }

    /** Applies thin borders to all four sides of a cell style. */
    private void applyBorder(XSSFCellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
    }

    /**
     * Writes the header row and auto-filter for a sheet, returns the styles to use in data rows.
     * The numeric column indices (0-based) receive right-aligned style.
     */
    private ExcelStyles createExcelHeader(XSSFWorkbook workbook, Sheet sheet, String[] headers) {
        ExcelStyles styles = buildStyles(workbook);
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(18);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.header);
        }
        // Enable auto-filter spanning all header columns
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, headers.length - 1));
        return styles;
    }

    /** Returns the correct data style (even/odd, left/right) for a given row index and column. */
    private XSSFCellStyle dataStyle(ExcelStyles styles, int rowNum, boolean rightAlign) {
        boolean isEven = (rowNum % 2 == 0);
        if (rightAlign) return isEven ? styles.rowEvenRight : styles.rowOddRight;
        return isEven ? styles.rowEven : styles.rowOdd;
    }

    /** Helper: sets a string cell with the appropriate row style. */
    private Cell styledCell(Row row, int col, String value, ExcelStyles styles, int rowNum) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(dataStyle(styles, rowNum, false));
        return cell;
    }

    /** Helper: sets a numeric cell (right-aligned) with the appropriate row style. */
    private Cell styledNumericCell(Row row, int col, double value, ExcelStyles styles, int rowNum) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(dataStyle(styles, rowNum, true));
        return cell;
    }

    private void addHeader(Document document, String title, String subtitle) throws Exception {
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 51, 102));
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, new Color(100, 100, 100));

        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        document.add(titlePara);

        Paragraph subPara = new Paragraph(subtitle, subtitleFont);
        subPara.setAlignment(Element.ALIGN_CENTER);
        document.add(subPara);
    }

    private void addSectionTitle(Document document, String title) throws Exception {
        Font font = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(0, 51, 102));
        Paragraph para = new Paragraph(title, font);
        para.setSpacingBefore(10);
        para.setSpacingAfter(5);
        document.add(para);
    }

    private void addBlankLines(Document document, int n) throws Exception {
        for (int i = 0; i < n; i++) {
            document.add(new Paragraph(" "));
        }
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label + ":", new Font(Font.HELVETICA, 10, Font.BOLD)));
        labelCell.setBackgroundColor(new Color(230, 230, 230));
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", new Font(Font.HELVETICA, 10)));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addFieldValue(Document document, String label, String value) throws Exception {
        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 10);

        Paragraph para = new Paragraph();
        para.add(new Phrase(label, labelFont));
        para.add(new Phrase(" " + value, valueFont));
        para.setSpacingAfter(5);
        document.add(para);
    }

    private void addPlainText(Document document, String text) throws Exception {
        Font font = new Font(Font.HELVETICA, 10);
        Paragraph para = new Paragraph(text, font);
        para.setSpacingAfter(5);
        document.add(para);
    }

    private void addFooter(Document document) throws Exception {
        Font font = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(150, 150, 150));

        Paragraph line = new Paragraph("____________________________________________________________", font);
        line.setAlignment(Element.ALIGN_CENTER);
        line.setSpacingBefore(20);
        document.add(line);

        Paragraph footer = new Paragraph(
                "Sistema de Saúde Ocupacional - Generated on " + LocalDateTime.now().format(DATE_FORMATTER),
                font);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private String formatTipoAtendimento(Atendimento.TipoAtendimento tipo) {
        return switch (tipo) {
            case CONSULTA_ROTINA -> "Consulta de Rotina";
            case EMERGENCIA -> "Emergência";
            case ACIDENTE_TRABALHO -> "Acidente de Trabalho";
            case RETORNO_TRABALHO -> "Retorno ao Trabalho";
            case EXAME_PERIODICO -> "Exame Periódico";
            case AVALIACAO_CLINICA -> "Avaliação Clínica";
        };
    }
}