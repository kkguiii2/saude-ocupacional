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
        
        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);
        String[] headers = { "ID", "Nome", "Matrícula", "Setor", "Cargo", "Tipo Risco", "Status", "Data Admissão", "EPI's" };
        
        int rowNum = createProfessionalHeader(sheet, styles, "Relatório de Colaboradores", colaboradores.size(), headers);
        
        int dataIndex = 0;
        for (Colaborador colab : colaboradores) {
            Row row = sheet.createRow(rowNum++);
            addCell(row, 0, colab.getId(), styles, dataIndex, "number");
            addCell(row, 1, colab.getNomeCompleto(), styles, dataIndex, "text");
            addCell(row, 2, colab.getMatricula(), styles, dataIndex, "text");
            addCell(row, 3, colab.getSetor() != null ? colab.getSetor().name() : null, styles, dataIndex, "text");
            addCell(row, 4, colab.getCargo(), styles, dataIndex, "text");
            addCell(row, 5, colab.getTipoRisco() != null ? colab.getTipoRisco().name() : null, styles, dataIndex, "text");
            addCell(row, 6, colab.getStatusFuncionario() != null ? colab.getStatusFuncionario().name() : null, styles, dataIndex, "text");
            addCell(row, 7, colab.getDataAdmissao(), styles, dataIndex, "date");
            addCell(row, 8, colab.getEpisObrigatorios(), styles, dataIndex, "text");
            dataIndex++;
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        addProfessionalFooter(sheet, styles, rowNum);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    public byte[] gerarExcelAtendimentos() {
        List<Atendimento> atendimentos = atendimentoRepository.findAll().stream().filter(Atendimento::isAtivo).toList();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Atendimentos");
        
        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);
        String[] headers = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Gravidade", "Emergência", "Sintomas", "Conduta", "Encaminhamento", "Profissional" };
        
        int rowNum = createProfessionalHeader(sheet, styles, "Relatório de Atendimentos", atendimentos.size(), headers);
        
        int dataIndex = 0;
        for (Atendimento at : atendimentos) {
            Row row = sheet.createRow(rowNum++);
            addCell(row, 0, at.getId(), styles, dataIndex, "number");
            addCell(row, 1, at.getDataHora(), styles, dataIndex, "date");
            addCell(row, 2, at.getColaborador() != null ? at.getColaborador().getNomeCompleto() : null, styles, dataIndex, "text");
            addCell(row, 3, at.getColaborador() != null ? at.getColaborador().getMatricula() : null, styles, dataIndex, "text");
            addCell(row, 4, at.getColaborador() != null && at.getColaborador().getSetor() != null ? at.getColaborador().getSetor().name() : null, styles, dataIndex, "text");
            addCell(row, 5, at.getTipo() != null ? formatTipoAtendimento(at.getTipo()) : null, styles, dataIndex, "text");
            addCell(row, 6, at.getGravidade() != null ? at.getGravidade().name() : null, styles, dataIndex, "text");
            addCell(row, 7, at.isEmergencia(), styles, dataIndex, "text");
            addCell(row, 8, at.getSintomas(), styles, dataIndex, "text");
            addCell(row, 9, at.getConduta(), styles, dataIndex, "text");
            addCell(row, 10, at.getEncaminhamento() != null ? at.getEncaminhamento().name() : null, styles, dataIndex, "text");
            addCell(row, 11, at.getAtendente() != null ? at.getAtendente().getNome() : null, styles, dataIndex, "text");
            dataIndex++;
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        addProfessionalFooter(sheet, styles, rowNum);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    public byte[] gerarExcelAgendamentos() {
        List<Agendamento> agendamentos = agendamentoRepository.findAll();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Agendamentos");
        
        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);
        String[] headers = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Status", "Observações", "Agendado Por" };
        
        int rowNum = createProfessionalHeader(sheet, styles, "Relatório de Agendamentos", agendamentos.size(), headers);
        
        int dataIndex = 0;
        for (Agendamento ag : agendamentos) {
            Row row = sheet.createRow(rowNum++);
            addCell(row, 0, ag.getId(), styles, dataIndex, "number");
            addCell(row, 1, ag.getDataHora(), styles, dataIndex, "date");
            addCell(row, 2, ag.getColaborador() != null ? ag.getColaborador().getNomeCompleto() : null, styles, dataIndex, "text");
            addCell(row, 3, ag.getColaborador() != null ? ag.getColaborador().getMatricula() : null, styles, dataIndex, "text");
            addCell(row, 4, ag.getColaborador() != null && ag.getColaborador().getSetor() != null ? ag.getColaborador().getSetor().name() : null, styles, dataIndex, "text");
            addCell(row, 5, ag.getTipo() != null ? ag.getTipo().name() : null, styles, dataIndex, "text");
            addCell(row, 6, ag.getStatus() != null ? ag.getStatus().name() : null, styles, dataIndex, "text");
            addCell(row, 7, ag.getObservacoes(), styles, dataIndex, "text");
            addCell(row, 8, ag.getAgendadoPor() != null ? ag.getAgendadoPor().getNome() : null, styles, dataIndex, "text");
            dataIndex++;
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        addProfessionalFooter(sheet, styles, rowNum);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    public byte[] gerarExcelEstoque() {
        List<Medicamento> medicamentos = medicamentoRepository.findAll();
        List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAll();
        XSSFWorkbook workbook = new XSSFWorkbook();
        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);
        
        Sheet sm = workbook.createSheet("Medicamentos");
        String[] h1 = { "ID", "Nome", "Princípio Ativo", "Categoria", "Quantidade", "Mínimo", "Unidade", "Validade", "Lote" };
        int rn = createProfessionalHeader(sm, styles, "Relatório de Estoque - Medicamentos", medicamentos.size(), h1);
        int dataIndex = 0;
        for (Medicamento med : medicamentos) {
            Row row = sm.createRow(rn++);
            addCell(row, 0, med.getId(), styles, dataIndex, "number");
            addCell(row, 1, med.getNome(), styles, dataIndex, "text");
            addCell(row, 2, med.getPrincipioAtivo(), styles, dataIndex, "text");
            addCell(row, 3, med.getCategoria() != null ? med.getCategoria().name() : null, styles, dataIndex, "text");
            addCell(row, 4, med.getQuantidadeEstoque(), styles, dataIndex, "number");
            addCell(row, 5, med.getQuantidadeMinima() != null ? med.getQuantidadeMinima() : 0, styles, dataIndex, "number");
            addCell(row, 6, med.getUnidade(), styles, dataIndex, "text");
            addCell(row, 7, med.getDataValidade(), styles, dataIndex, "date");
            addCell(row, 8, med.getLote(), styles, dataIndex, "text");
            dataIndex++;
        }
        for (int i = 0; i < h1.length; i++) sm.autoSizeColumn(i);
        addProfessionalFooter(sm, styles, rn);

        Sheet sv = workbook.createSheet("Movimentações");
        String[] h2 = { "ID", "Data", "Tipo", "Medicamento", "Quantidade", "Descrição", "Usuário" };
        rn = createProfessionalHeader(sv, styles, "Relatório de Estoque - Movimentações", movimentacoes.size(), h2);
        dataIndex = 0;
        for (MovimentacaoEstoque mov : movimentacoes) {
            Row row = sv.createRow(rn++);
            addCell(row, 0, mov.getId(), styles, dataIndex, "number");
            addCell(row, 1, mov.getDataHora(), styles, dataIndex, "date");
            addCell(row, 2, mov.getTipo() != null ? mov.getTipo().name() : null, styles, dataIndex, "text");
            addCell(row, 3, mov.getMedicamento() != null ? mov.getMedicamento().getNome() : null, styles, dataIndex, "text");
            addCell(row, 4, mov.getQuantidade(), styles, dataIndex, "number");
            addCell(row, 5, mov.getMotivo(), styles, dataIndex, "text");
            addCell(row, 6, mov.getResponsavel() != null ? mov.getResponsavel().getNome() : null, styles, dataIndex, "text");
            dataIndex++;
        }
        for (int i = 0; i < h2.length; i++) sv.autoSizeColumn(i);
        addProfessionalFooter(sv, styles, rn);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    public byte[] gerarExcelAcidentes() {
        List<AcidenteTrabalho> acidentes = acidenteTrabalhoRepository.findAll();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Acidentes de Trabalho");
        
        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);
        String[] headers = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Local", "Descrição", "Causa", "CAT Emitida", "Nº CAT", "Registrado Por" };
        
        int rowNum = createProfessionalHeader(sheet, styles, "Relatório de Acidentes de Trabalho", acidentes.size(), headers);
        
        int dataIndex = 0;
        for (AcidenteTrabalho ac : acidentes) {
            Row row = sheet.createRow(rowNum++);
            addCell(row, 0, ac.getId(), styles, dataIndex, "number");
            addCell(row, 1, ac.getDataHora(), styles, dataIndex, "date");
            addCell(row, 2, ac.getColaborador() != null ? ac.getColaborador().getNomeCompleto() : null, styles, dataIndex, "text");
            addCell(row, 3, ac.getColaborador() != null ? ac.getColaborador().getMatricula() : null, styles, dataIndex, "text");
            addCell(row, 4, ac.getColaborador() != null && ac.getColaborador().getSetor() != null ? ac.getColaborador().getSetor().name() : null, styles, dataIndex, "text");
            addCell(row, 5, ac.getTipo() != null ? ac.getTipo().name() : null, styles, dataIndex, "text");
            addCell(row, 6, ac.getLocalFabrica(), styles, dataIndex, "text");
            addCell(row, 7, ac.getDescricao(), styles, dataIndex, "text");
            addCell(row, 8, ac.getCausa(), styles, dataIndex, "text");
            addCell(row, 9, ac.isCatEmitida(), styles, dataIndex, "text");
            addCell(row, 10, ac.getNumeroCat(), styles, dataIndex, "text");
            addCell(row, 11, ac.getRegistradoPor() != null ? ac.getRegistradoPor().getNome() : null, styles, dataIndex, "text");
            dataIndex++;
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        addProfessionalFooter(sheet, styles, rowNum);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel de acidentes: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    public byte[] gerarExcelCompleto(List<String> modulos) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);

        if (modulos.contains("colaboradores")) {
            List<Colaborador> colaboradores = colaboradorRepository.findAll();
            Sheet sheet = workbook.createSheet("Colaboradores");
            String[] h = { "ID", "Nome", "Matrícula", "Setor", "Cargo", "Tipo Risco", "Status", "Data Admissão", "EPI's" };
            int rn = createProfessionalHeader(sheet, styles, "Relatório de Colaboradores", colaboradores.size(), h);
            int dataIndex = 0;
            for (Colaborador c : colaboradores) {
                Row row = sheet.createRow(rn++);
                addCell(row, 0, c.getId(), styles, dataIndex, "number");
                addCell(row, 1, c.getNomeCompleto(), styles, dataIndex, "text");
                addCell(row, 2, c.getMatricula(), styles, dataIndex, "text");
                addCell(row, 3, c.getSetor() != null ? c.getSetor().name() : null, styles, dataIndex, "text");
                addCell(row, 4, c.getCargo(), styles, dataIndex, "text");
                addCell(row, 5, c.getTipoRisco() != null ? c.getTipoRisco().name() : null, styles, dataIndex, "text");
                addCell(row, 6, c.getStatusFuncionario() != null ? c.getStatusFuncionario().name() : null, styles, dataIndex, "text");
                addCell(row, 7, c.getDataAdmissao(), styles, dataIndex, "date");
                addCell(row, 8, c.getEpisObrigatorios(), styles, dataIndex, "text");
                dataIndex++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
            addProfessionalFooter(sheet, styles, rn);
        }

        if (modulos.contains("atendimentos")) {
            List<Atendimento> atendimentos = atendimentoRepository.findAll().stream().filter(Atendimento::isAtivo).toList();
            Sheet sheet = workbook.createSheet("Atendimentos");
            String[] h = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Gravidade", "Emergência", "Sintomas", "Conduta", "Encaminhamento", "Profissional" };
            int rn = createProfessionalHeader(sheet, styles, "Relatório de Atendimentos", atendimentos.size(), h);
            int dataIndex = 0;
            for (Atendimento at : atendimentos) {
                Row row = sheet.createRow(rn++);
                addCell(row, 0, at.getId(), styles, dataIndex, "number");
                addCell(row, 1, at.getDataHora(), styles, dataIndex, "date");
                addCell(row, 2, at.getColaborador() != null ? at.getColaborador().getNomeCompleto() : null, styles, dataIndex, "text");
                addCell(row, 3, at.getColaborador() != null ? at.getColaborador().getMatricula() : null, styles, dataIndex, "text");
                addCell(row, 4, at.getColaborador() != null && at.getColaborador().getSetor() != null ? at.getColaborador().getSetor().name() : null, styles, dataIndex, "text");
                addCell(row, 5, at.getTipo() != null ? formatTipoAtendimento(at.getTipo()) : null, styles, dataIndex, "text");
                addCell(row, 6, at.getGravidade() != null ? at.getGravidade().name() : null, styles, dataIndex, "text");
                addCell(row, 7, at.isEmergencia(), styles, dataIndex, "text");
                addCell(row, 8, at.getSintomas(), styles, dataIndex, "text");
                addCell(row, 9, at.getConduta(), styles, dataIndex, "text");
                addCell(row, 10, at.getEncaminhamento() != null ? at.getEncaminhamento().name() : null, styles, dataIndex, "text");
                addCell(row, 11, at.getAtendente() != null ? at.getAtendente().getNome() : null, styles, dataIndex, "text");
                dataIndex++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
            addProfessionalFooter(sheet, styles, rn);
        }

        if (modulos.contains("agendamentos")) {
            List<Agendamento> agendamentos = agendamentoRepository.findAll();
            Sheet sheet = workbook.createSheet("Agendamentos");
            String[] h = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Status", "Observações", "Agendado Por" };
            int rn = createProfessionalHeader(sheet, styles, "Relatório de Agendamentos", agendamentos.size(), h);
            int dataIndex = 0;
            for (Agendamento ag : agendamentos) {
                Row row = sheet.createRow(rn++);
                addCell(row, 0, ag.getId(), styles, dataIndex, "number");
                addCell(row, 1, ag.getDataHora(), styles, dataIndex, "date");
                addCell(row, 2, ag.getColaborador() != null ? ag.getColaborador().getNomeCompleto() : null, styles, dataIndex, "text");
                addCell(row, 3, ag.getColaborador() != null ? ag.getColaborador().getMatricula() : null, styles, dataIndex, "text");
                addCell(row, 4, ag.getColaborador() != null && ag.getColaborador().getSetor() != null ? ag.getColaborador().getSetor().name() : null, styles, dataIndex, "text");
                addCell(row, 5, ag.getTipo() != null ? ag.getTipo().name() : null, styles, dataIndex, "text");
                addCell(row, 6, ag.getStatus() != null ? ag.getStatus().name() : null, styles, dataIndex, "text");
                addCell(row, 7, ag.getObservacoes(), styles, dataIndex, "text");
                addCell(row, 8, ag.getAgendadoPor() != null ? ag.getAgendadoPor().getNome() : null, styles, dataIndex, "text");
                dataIndex++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
            addProfessionalFooter(sheet, styles, rn);
        }

        if (modulos.contains("acidentes")) {
            List<AcidenteTrabalho> acidentes = acidenteTrabalhoRepository.findAll();
            Sheet sheet = workbook.createSheet("Acidentes de Trabalho");
            String[] h = { "ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Local", "Descrição", "Causa", "CAT Emitida", "Nº CAT", "Registrado Por" };
            int rn = createProfessionalHeader(sheet, styles, "Relatório de Acidentes de Trabalho", acidentes.size(), h);
            int dataIndex = 0;
            for (AcidenteTrabalho ac : acidentes) {
                Row row = sheet.createRow(rn++);
                addCell(row, 0, ac.getId(), styles, dataIndex, "number");
                addCell(row, 1, ac.getDataHora(), styles, dataIndex, "date");
                addCell(row, 2, ac.getColaborador() != null ? ac.getColaborador().getNomeCompleto() : null, styles, dataIndex, "text");
                addCell(row, 3, ac.getColaborador() != null ? ac.getColaborador().getMatricula() : null, styles, dataIndex, "text");
                addCell(row, 4, ac.getColaborador() != null && ac.getColaborador().getSetor() != null ? ac.getColaborador().getSetor().name() : null, styles, dataIndex, "text");
                addCell(row, 5, ac.getTipo() != null ? ac.getTipo().name() : null, styles, dataIndex, "text");
                addCell(row, 6, ac.getLocalFabrica(), styles, dataIndex, "text");
                addCell(row, 7, ac.getDescricao(), styles, dataIndex, "text");
                addCell(row, 8, ac.getCausa(), styles, dataIndex, "text");
                addCell(row, 9, ac.isCatEmitida(), styles, dataIndex, "text");
                addCell(row, 10, ac.getNumeroCat(), styles, dataIndex, "text");
                addCell(row, 11, ac.getRegistradoPor() != null ? ac.getRegistradoPor().getNome() : null, styles, dataIndex, "text");
                dataIndex++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
            addProfessionalFooter(sheet, styles, rn);
        }

        if (modulos.contains("medicamentos")) {
            List<Medicamento> medicamentos = medicamentoRepository.findAll();
            Sheet sheet = workbook.createSheet("Medicamentos");
            String[] h = { "ID", "Nome", "Princípio Ativo", "Categoria", "Quantidade", "Mínimo", "Unidade", "Validade", "Lote" };
            int rn = createProfessionalHeader(sheet, styles, "Relatório de Estoque - Medicamentos", medicamentos.size(), h);
            int dataIndex = 0;
            for (Medicamento med : medicamentos) {
                Row row = sheet.createRow(rn++);
                addCell(row, 0, med.getId(), styles, dataIndex, "number");
                addCell(row, 1, med.getNome(), styles, dataIndex, "text");
                addCell(row, 2, med.getPrincipioAtivo(), styles, dataIndex, "text");
                addCell(row, 3, med.getCategoria() != null ? med.getCategoria().name() : null, styles, dataIndex, "text");
                addCell(row, 4, med.getQuantidadeEstoque(), styles, dataIndex, "number");
                addCell(row, 5, med.getQuantidadeMinima() != null ? med.getQuantidadeMinima() : 0, styles, dataIndex, "number");
                addCell(row, 6, med.getUnidade(), styles, dataIndex, "text");
                addCell(row, 7, med.getDataValidade(), styles, dataIndex, "date");
                addCell(row, 8, med.getLote(), styles, dataIndex, "text");
                dataIndex++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
            addProfessionalFooter(sheet, styles, rn);
        }

        if (modulos.contains("movimentacoes")) {
            List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAll();
            Sheet sheet = workbook.createSheet("Movimentações Estoque");
            String[] h = { "ID", "Data", "Tipo", "Medicamento", "Quantidade", "Motivo", "Responsável" };
            int rn = createProfessionalHeader(sheet, styles, "Relatório de Estoque - Movimentações", movimentacoes.size(), h);
            int dataIndex = 0;
            for (MovimentacaoEstoque mov : movimentacoes) {
                Row row = sheet.createRow(rn++);
                addCell(row, 0, mov.getId(), styles, dataIndex, "number");
                addCell(row, 1, mov.getDataHora(), styles, dataIndex, "date");
                addCell(row, 2, mov.getTipo() != null ? mov.getTipo().name() : null, styles, dataIndex, "text");
                addCell(row, 3, mov.getMedicamento() != null ? mov.getMedicamento().getNome() : null, styles, dataIndex, "text");
                addCell(row, 4, mov.getQuantidade(), styles, dataIndex, "number");
                addCell(row, 5, mov.getMotivo(), styles, dataIndex, "text");
                addCell(row, 6, mov.getResponsavel() != null ? mov.getResponsavel().getNome() : null, styles, dataIndex, "text");
                dataIndex++;
            }
            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);
            addProfessionalFooter(sheet, styles, rn);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel completo: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    public byte[] gerarExcelNR7() {
        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório PCMSO (NR-7 e eSocial)");
        
        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);
        String[] headers = {
            "ID", "Nome do Colaborador", "Matrícula", "PIS/PASEP", "Setor", "Cargo",
            "Data Admissão", "Risco Ocupacional", "Último Exame (ASO)", "Próximo Exame",
            "Restrições", "Observações Médicas"
        };
        
        int rowNum = createProfessionalHeader(sheet, styles, "Relatório PCMSO (NR-7 e eSocial)", colaboradores.size(), headers);
        
        int dataIndex = 0;
        for (Colaborador colab : colaboradores) {
            Row row = sheet.createRow(rowNum++);
            addCell(row, 0, colab.getId(), styles, dataIndex, "number");
            addCell(row, 1, colab.getNomeCompleto(), styles, dataIndex, "text");
            addCell(row, 2, colab.getMatricula(), styles, dataIndex, "text");
            addCell(row, 3, colab.getPisPasep(), styles, dataIndex, "text");
            addCell(row, 4, colab.getSetor() != null ? colab.getSetor().name() : null, styles, dataIndex, "text");
            addCell(row, 5, colab.getCargo(), styles, dataIndex, "text");
            addCell(row, 6, colab.getDataAdmissao(), styles, dataIndex, "date");
            addCell(row, 7, colab.getTipoRisco() != null ? colab.getTipoRisco().name() : null, styles, dataIndex, "text");
            
            ProntuarioOcupacional p = colab.getProntuario();
            if (p != null) {
                addCell(row, 8, p.getUltimoExame(), styles, dataIndex, "date");
                addCell(row, 9, p.getProximoExame(), styles, dataIndex, "date");
                addCell(row, 10, p.getRestricoesTrabalho(), styles, dataIndex, "text");
                String obs = "";
                if (p.getAlergias() != null) obs += "Alergias: " + p.getAlergias() + " ";
                if (p.getMedicacoesUso() != null) obs += "Med: " + p.getMedicacoesUso();
                addCell(row, 11, obs.trim(), styles, dataIndex, "text");
            } else {
                addCell(row, 8, null, styles, dataIndex, "date");
                addCell(row, 9, null, styles, dataIndex, "date");
                addCell(row, 10, null, styles, dataIndex, "text");
                addCell(row, 11, "Sem Prontuário", styles, dataIndex, "text");
            }
            dataIndex++;
        }
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
        addProfessionalFooter(sheet, styles, rowNum);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error("Erro ao gerar Excel NR-7: {}", e.getMessage()); }
        return baos.toByteArray();
    }

    private static class ExcelProfessionalStyles {
        final XSSFCellStyle title;
        final XSSFCellStyle info;
        final XSSFCellStyle header;
        final XSSFCellStyle rowEvenLeft;
        final XSSFCellStyle rowOddLeft;
        final XSSFCellStyle rowEvenRight;
        final XSSFCellStyle rowOddRight;
        final XSSFCellStyle rowEvenCenter;
        final XSSFCellStyle rowOddCenter;
        final XSSFCellStyle footer;

        ExcelProfessionalStyles(XSSFWorkbook wb) {
            title = wb.createCellStyle();
            XSSFFont titleFont = wb.createFont();
            titleFont.setFontName("Arial");
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setBold(true);
            title.setFont(titleFont);
            title.setAlignment(HorizontalAlignment.CENTER);

            info = wb.createCellStyle();
            XSSFFont infoFont = wb.createFont();
            infoFont.setFontName("Arial");
            infoFont.setFontHeightInPoints((short) 10);
            info.setFont(infoFont);
            info.setAlignment(HorizontalAlignment.LEFT);

            header = wb.createCellStyle();
            XSSFFont headerFont = wb.createFont();
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 10);
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            header.setFont(headerFont);
            header.setFillForegroundColor(new XSSFColor(new byte[]{(byte)0x40, (byte)0x40, (byte)0x40}, null));
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            applyBorder(header);

            XSSFFont dataFont = wb.createFont();
            dataFont.setFontName("Arial");
            dataFont.setFontHeightInPoints((short) 10);

            XSSFColor colorWhite = new XSSFColor(new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF}, null);
            XSSFColor colorZebra = new XSSFColor(new byte[]{(byte)0xF5, (byte)0xF5, (byte)0xF5}, null);

            rowEvenLeft = createDataStyle(wb, dataFont, colorWhite, HorizontalAlignment.LEFT);
            rowOddLeft = createDataStyle(wb, dataFont, colorZebra, HorizontalAlignment.LEFT);

            rowEvenRight = createDataStyle(wb, dataFont, colorWhite, HorizontalAlignment.RIGHT);
            rowOddRight = createDataStyle(wb, dataFont, colorZebra, HorizontalAlignment.RIGHT);

            rowEvenCenter = createDataStyle(wb, dataFont, colorWhite, HorizontalAlignment.CENTER);
            rowOddCenter = createDataStyle(wb, dataFont, colorZebra, HorizontalAlignment.CENTER);

            footer = wb.createCellStyle();
            XSSFFont footerFont = wb.createFont();
            footerFont.setFontName("Arial");
            footerFont.setFontHeightInPoints((short) 9);
            footerFont.setItalic(true);
            footer.setFont(footerFont);
            footer.setAlignment(HorizontalAlignment.LEFT);
        }

        private XSSFCellStyle createDataStyle(XSSFWorkbook wb, XSSFFont font, XSSFColor bgColor, HorizontalAlignment align) {
            XSSFCellStyle style = wb.createCellStyle();
            style.setFont(font);
            style.setFillForegroundColor(bgColor);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(align);
            style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            style.setWrapText(true);
            applyBorder(style);
            return style;
        }

        private void applyBorder(XSSFCellStyle style) {
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
        }
    }

    private int createProfessionalHeader(Sheet sheet, ExcelProfessionalStyles styles, String titleText, int totalRegistros, String[] headers) {
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(titleText);
        titleCell.setCellStyle(styles.title);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));
        
        Row infoRow1 = sheet.createRow(2);
        Cell infoCell1 = infoRow1.createCell(0);
        infoCell1.setCellValue("Data de geração: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        infoCell1.setCellStyle(styles.info);

        Row infoRow2 = sheet.createRow(3);
        Cell infoCell2 = infoRow2.createCell(0);
        infoCell2.setCellValue("Total de registros: " + totalRegistros);
        infoCell2.setCellStyle(styles.info);

        Row headerRow = sheet.createRow(5);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.header);
        }
        sheet.setAutoFilter(new CellRangeAddress(5, 5, 0, headers.length - 1));

        return 6; 
    }

    private void addProfessionalFooter(Sheet sheet, ExcelProfessionalStyles styles, int currentRow) {
        Row footerRow = sheet.createRow(currentRow + 1);
        Cell footerCell = footerRow.createCell(0);
        footerCell.setCellValue("Relatório gerado automaticamente pelo sistema de Saúde Ocupacional");
        footerCell.setCellStyle(styles.footer);
    }

    private void addCell(Row row, int col, Object value, ExcelProfessionalStyles styles, int dataIndex, String formatType) {
        Cell cell = row.createCell(col);
        boolean isEven = (dataIndex % 2 == 0);
        
        String strValue = "-";
        
        if (value != null) {
            if (value instanceof String) {
                if (!((String) value).trim().isEmpty()) {
                    strValue = (String) value;
                }
            } else if (value instanceof Boolean) {
                strValue = (Boolean) value ? "Sim" : "Não";
            } else if (value instanceof LocalDateTime) {
                strValue = ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else if (value instanceof java.time.LocalDate) {
                strValue = ((java.time.LocalDate) value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else if (value instanceof Enum) {
                strValue = ((Enum<?>) value).name();
            } else {
                strValue = value.toString();
                if (strValue.trim().isEmpty()) {
                    strValue = "-";
                }
            }
        }
        
        if ("currency".equals(formatType) && value instanceof Number) {
            strValue = String.format("R$ %,.2f", ((Number) value).doubleValue());
        }
        
        if (strValue.trim().isEmpty()) {
            strValue = "-";
        }
        cell.setCellValue(strValue);

        if ("number".equals(formatType) || "currency".equals(formatType) || value instanceof Number) {
            cell.setCellStyle(isEven ? styles.rowEvenRight : styles.rowOddRight);
        } else if ("date".equals(formatType) || value instanceof LocalDateTime || value instanceof java.time.LocalDate) {
            cell.setCellStyle(isEven ? styles.rowEvenCenter : styles.rowOddCenter);
        } else {
            cell.setCellStyle(isEven ? styles.rowEvenLeft : styles.rowOddLeft);
        }
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
