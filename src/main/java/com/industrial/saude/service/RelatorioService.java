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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
        table.setWidths(new float[]{30, 70});

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
        colabTable.setWidths(new float[]{30, 70});

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
        profTable.setWidths(new float[]{30, 70});

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
        dadosTable.setWidths(new float[]{30, 70});

        addRow(dadosTable, "Nome", colab.getNomeCompleto());
        addRow(dadosTable, "Matrícula", colab.getMatricula());
        addRow(dadosTable, "Setor", colab.getSetor().name());
        addRow(dadosTable, "Cargo", colab.getCargo());
        addRow(dadosTable, "Data de Admissão", colab.getDataAdmissao() != null ? colab.getDataAdmissao().format(DATE_ONLY) : "N/A");
        addRow(dadosTable, "Status", colab.getStatusFuncionario().name());
        addRow(dadosTable, "Tipo de Risco", colab.getTipoRisco().name());
        addRow(dadosTable, "EPI's Obrigatórios", colab.getEpisObrigatorios() != null ? colab.getEpisObrigatorios() : "Nenhum");
        addRow(dadosTable, "Contato de Emergência", colab.getContatoEmergencia() != null ? colab.getContatoEmergencia() : "N/A");

        document.add(dadosTable);

        addBlankLines(document, 1);

        addSectionTitle(document, "HISTÓRICO DE ATENDIMENTOS (" + atendimentos.size() + ")");

        if (atendimentos.isEmpty()) {
            addPlainText(document, "Nenhum atendimento registrado.");
        } else {
            for (Atendimento a : atendimentos) {
                PdfPTable atTable = new PdfPTable(2);
                atTable.setWidthPercentage(100);
                atTable.setWidths(new float[]{30, 70});

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
                acTable.setWidths(new float[]{30, 70});

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
                agTable.setWidths(new float[]{30, 70});

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

        CellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Nome", "Matrícula", "Setor", "Cargo", "Tipo Risco", "Status", "Data Admissão", "EPI's"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Colaborador colab : colaboradores) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(colab.getId());
            row.createCell(1).setCellValue(colab.getNomeCompleto());
            row.createCell(2).setCellValue(colab.getMatricula());
            row.createCell(3).setCellValue(colab.getSetor().name());
            row.createCell(4).setCellValue(colab.getCargo());
            row.createCell(5).setCellValue(colab.getTipoRisco().name());
            row.createCell(6).setCellValue(colab.getStatusFuncionario().name());
            row.createCell(7).setCellValue(colab.getDataAdmissao() != null ? colab.getDataAdmissao().toString() : "");
            row.createCell(8).setCellValue(colab.getEpisObrigatorios() != null ? colab.getEpisObrigatorios() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            workbook.close();
        } catch (IOException e) {
            log.error("Erro ao gerar Excel: {}", e.getMessage());
        }

        return baos.toByteArray();
    }

    public byte[] gerarExcelAtendimentos() {
        List<Atendimento> atendimentos = atendimentoRepository.findAll().stream()
                .filter(Atendimento::isAtivo)
                .toList();

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Atendimentos");

        CellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Gravidade", "Emergência", "Sintomas", "Conduta", "Encaminhamento", "Profissional"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Atendimento at : atendimentos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(at.getId());
            row.createCell(1).setCellValue(at.getDataHora().format(DATE_FORMATTER));
            row.createCell(2).setCellValue(at.getColaborador().getNomeCompleto());
            row.createCell(3).setCellValue(at.getColaborador().getMatricula());
            row.createCell(4).setCellValue(at.getColaborador().getSetor().name());
            row.createCell(5).setCellValue(formatTipoAtendimento(at.getTipo()));
            row.createCell(6).setCellValue(at.getGravidade().name());
            row.createCell(7).setCellValue(at.isEmergencia() ? "SIM" : "NÃO");
            row.createCell(8).setCellValue(at.getSintomas() != null ? at.getSintomas() : "");
            row.createCell(9).setCellValue(at.getConduta() != null ? at.getConduta() : "");
            row.createCell(10).setCellValue(at.getEncaminhamento() != null ? at.getEncaminhamento().name() : "");
            row.createCell(11).setCellValue(at.getAtendente().getNome());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            workbook.close();
        } catch (IOException e) {
            log.error("Erro ao gerar Excel: {}", e.getMessage());
        }

        return baos.toByteArray();
    }

    public byte[] gerarExcelAgendamentos() {
        List<Agendamento> agendamentos = agendamentoRepository.findAll();

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Agendamentos");

        CellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Data/Hora", "Colaborador", "Matrícula", "Setor", "Tipo", "Status", "Observações", "Agendado Por"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Agendamento ag : agendamentos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(ag.getId());
            row.createCell(1).setCellValue(ag.getDataHora().format(DATE_FORMATTER));
            row.createCell(2).setCellValue(ag.getColaborador().getNomeCompleto());
            row.createCell(3).setCellValue(ag.getColaborador().getMatricula());
            row.createCell(4).setCellValue(ag.getColaborador().getSetor().name());
            row.createCell(5).setCellValue(ag.getTipo().name());
            row.createCell(6).setCellValue(ag.getStatus().name());
            row.createCell(7).setCellValue(ag.getObservacoes() != null ? ag.getObservacoes() : "");
            row.createCell(8).setCellValue(ag.getAgendadoPor() != null ? ag.getAgendadoPor().getNome() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            workbook.close();
        } catch (IOException e) {
            log.error("Erro ao gerar Excel: {}", e.getMessage());
        }

        return baos.toByteArray();
    }

    public byte[] gerarExcelEstoque() {
        List<Medicamento> medicamentos = medicamentoRepository.findAll();
        List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAll();

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheetMedicamentos = workbook.createSheet("Medicamentos");
        Sheet sheetMovimentacoes = workbook.createSheet("Movimentações");

        createExcelHeader(workbook, sheetMedicamentos, new String[]{"ID", "Nome", "Princípio Ativo", "Categoria", "Quantidade", "Mínimo", "Unidade", "Validade", "Lote"});
        int rowNum = 1;
        for (Medicamento med : medicamentos) {
            Row row = sheetMedicamentos.createRow(rowNum++);
            row.createCell(0).setCellValue(med.getId());
            row.createCell(1).setCellValue(med.getNome());
            row.createCell(2).setCellValue(med.getPrincipioAtivo() != null ? med.getPrincipioAtivo() : "");
            row.createCell(3).setCellValue(med.getCategoria() != null ? med.getCategoria().name() : "");
            row.createCell(4).setCellValue(med.getQuantidadeEstoque());
            row.createCell(5).setCellValue(med.getQuantidadeMinima() != null ? med.getQuantidadeMinima() : 0);
            row.createCell(6).setCellValue(med.getUnidade() != null ? med.getUnidade() : "");
            row.createCell(7).setCellValue(med.getDataValidade() != null ? med.getDataValidade().format(DATE_FORMATTER) : "");
            row.createCell(8).setCellValue(med.getLote() != null ? med.getLote() : "");
        }

        createExcelHeader(workbook, sheetMovimentacoes, new String[]{"ID", "Data", "Tipo", "Medicamento", "Quantidade", "Descrição", "Usuário"});
        rowNum = 1;
        for (MovimentacaoEstoque mov : movimentacoes) {
            Row row = sheetMovimentacoes.createRow(rowNum++);
            row.createCell(0).setCellValue(mov.getId());
            row.createCell(1).setCellValue(mov.getDataHora().format(DATE_FORMATTER));
            row.createCell(2).setCellValue(mov.getTipo().name());
            row.createCell(3).setCellValue(mov.getMedicamento().getNome());
            row.createCell(4).setCellValue(mov.getQuantidade());
            row.createCell(5).setCellValue(mov.getMotivo() != null ? mov.getMotivo() : "");
            row.createCell(6).setCellValue(mov.getResponsavel() != null ? mov.getResponsavel().getNome() : "");
        }

        sheetMedicamentos.autoSizeColumn(0);
        sheetMedicamentos.autoSizeColumn(1);
        sheetMedicamentos.autoSizeColumn(2);
        sheetMedicamentos.autoSizeColumn(3);
        sheetMedicamentos.autoSizeColumn(4);
        sheetMovimentacoes.autoSizeColumn(0);
        sheetMovimentacoes.autoSizeColumn(1);
        sheetMovimentacoes.autoSizeColumn(2);
        sheetMovimentacoes.autoSizeColumn(3);
        sheetMovimentacoes.autoSizeColumn(4);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            workbook.close();
        } catch (IOException e) {
            log.error("Erro ao gerar Excel: {}", e.getMessage());
        }

        return baos.toByteArray();
    }

    private void createExcelHeader(XSSFWorkbook workbook, Sheet sheet, String[] headers) {
        CellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
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
            font
        );
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