package com.industrial.saude.service;

import com.industrial.saude.dto.AcidenteTrabalhoDTO;
import com.industrial.saude.model.AcidenteTrabalho;
import com.industrial.saude.model.Colaborador;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.repository.AcidenteTrabalhoRepository;
import com.industrial.saude.repository.ColaboradorRepository;
import com.industrial.saude.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcidenteService {

    private final AcidenteTrabalhoRepository repository;
    private final ColaboradorRepository colaboradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    
    public List<AcidenteTrabalhoDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<AcidenteTrabalhoDTO> findByColaborador(Long colaboradorId) {
        return repository.findByColaboradorId(colaboradorId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<AcidenteTrabalhoDTO> findNaoEmitida() {
        return repository.findByCatEmitidaAndAtivoTrue(false).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<AcidenteTrabalhoDTO> findMes() {
        LocalDateTime inicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fim = LocalDate.now().atTime(LocalTime.MAX);
        return repository.findByPeriodo(inicio, fim).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public AcidenteTrabalhoDTO findById(Long id) {
        return repository.findById(id).map(this::toDTO).orElse(null);
    }
    
    @Transactional
    public AcidenteTrabalhoDTO save(AcidenteTrabalhoDTO dto, Long usuarioId) {
        AcidenteTrabalho entity = new AcidenteTrabalho();
        
        Colaborador colaborador;
        if (dto.getColaboradorMatricula() != null && !dto.getColaboradorMatricula().isEmpty()) {
            colaborador = colaboradorRepository.findByMatricula(dto.getColaboradorMatricula())
                    .orElseThrow(() -> new RuntimeException("Colaborador não encontrado com a matrícula: " + dto.getColaboradorMatricula()));
        } else {
            throw new RuntimeException("Matrícula do colaborador é obrigatória");
        }
        entity.setColaborador(colaborador);
        
        entity.setDataHora(LocalDateTime.parse(dto.getDataHora()));
        entity.setLocalFabrica(dto.getLocalFabrica());
        entity.setTipo(dto.getTipo());
        entity.setDescricao(dto.getDescricao());
        entity.setCausa(dto.getCausa());
        entity.setMedidasTomadas(dto.getMedidasTomadas());
        entity.setTestemunhas(dto.getTestemunhas());
        entity.setCatEmitida(dto.isCatEmitida());
        entity.setNumeroCat(dto.getNumeroCat());
        if (dto.getDataCat() != null && !dto.getDataCat().isEmpty()) {
            entity.setDataCat(LocalDateTime.parse(dto.getDataCat()));
        }
        entity.setCnpjEmpresa("12.345.678/0001-95");
        entity.setCid(dto.getCid());
        entity.setParteCorpoAtingida(dto.getParteCorpoAtingida());
        entity.setDiasAfastados(dto.getDiasAfastados());
        entity.setDataCadastro(LocalDateTime.now());
        
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        entity.setRegistradoPor(usuario);

        entity = repository.save(entity);

        String username = usuario != null ? usuario.getUsername() : "sistema";
        auditoriaService.registrar(username, AuditoriaService.ACAO_CREATE, "ACIDENTE",
                "Acidente registrado para: " + entity.getColaborador().getNomeCompleto());

        return toDTO(entity);
    }

    @Transactional
    public AcidenteTrabalhoDTO update(Long id, AcidenteTrabalhoDTO dto) {
        AcidenteTrabalho entity = repository.findById(id).orElse(null);
        if (entity == null) return null;
        
        entity.setLocalFabrica(dto.getLocalFabrica());
        entity.setTipo(dto.getTipo());
        entity.setDescricao(dto.getDescricao());
        entity.setCausa(dto.getCausa());
        entity.setMedidasTomadas(dto.getMedidasTomadas());
        entity.setTestemunhas(dto.getTestemunhas());
        entity.setCatEmitida(dto.isCatEmitida());
        entity.setNumeroCat(dto.getNumeroCat());
        if (dto.getDataCat() != null && !dto.getDataCat().isEmpty()) {
            entity.setDataCat(LocalDateTime.parse(dto.getDataCat()));
        }
        entity.setCnpjEmpresa("12.345.678/0001-95");
        entity.setCid(dto.getCid());
        entity.setParteCorpoAtingida(dto.getParteCorpoAtingida());
        entity.setDiasAfastados(dto.getDiasAfastados());

        repository.save(entity);

        String username = entity.getRegistradoPor() != null ? entity.getRegistradoPor().getUsername() : "sistema";
        auditoriaService.registrar(username, AuditoriaService.ACAO_UPDATE, "ACIDENTE",
                "Acidente atualizado ID: " + id);

        return toDTO(entity);
    }

    @Transactional
    public byte[] emitirCat(Long id) {
        AcidenteTrabalho entity = repository.findById(id).orElse(null);
        if (entity == null) return null;

        if (!entity.isCatEmitida()) {
            entity.setCatEmitida(true);
            entity.setNumeroCat("CAT-" + System.currentTimeMillis());
            entity.setDataCat(LocalDateTime.now());
            repository.save(entity);

            String username = entity.getRegistradoPor() != null ? entity.getRegistradoPor().getUsername() : "sistema";
            auditoriaService.registrar(username, AuditoriaService.ACAO_UPDATE, "ACIDENTE",
                    "CAT emitida ID: " + id + " - " + entity.getNumeroCat());
        }

        return gerarExcelCat(entity);
    }

    // ─── Excel CAT (Layout Oficial INSS) ──────────────────────────────────────

    private byte[] gerarExcelCat(AcidenteTrabalho entity) {
        DateTimeFormatter dtFmt  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter dFmt   = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Colaborador colab = entity.getColaborador();

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("CAT");
            
            // Grid de 10 colunas: Col 0 para label vertical, Cols 1 a 9 para dados
            sheet.setColumnWidth(0, 5 * 256);
            for(int i=1; i<=9; i++) {
                sheet.setColumnWidth(i, 11 * 256);
            }

            // ── Fontes
            XSSFFont fontHeader = wb.createFont();
            fontHeader.setFontName("Arial");
            fontHeader.setFontHeightInPoints((short) 12);
            fontHeader.setBold(true);

            XSSFFont fontLabel = wb.createFont();
            fontLabel.setFontName("Arial");
            fontLabel.setFontHeightInPoints((short) 7);
            fontLabel.setColor(IndexedColors.GREY_80_PERCENT.getIndex());

            XSSFFont fontValue = wb.createFont();
            fontValue.setFontName("Arial");
            fontValue.setFontHeightInPoints((short) 9);
            fontValue.setBold(true);

            XSSFFont fontVertical = wb.createFont();
            fontVertical.setFontName("Arial");
            fontVertical.setFontHeightInPoints((short) 8);
            fontVertical.setBold(true);

            // ── Estilos
            XSSFCellStyle styleHeader = wb.createCellStyle();
            styleHeader.setAlignment(HorizontalAlignment.CENTER);
            styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            styleHeader.setFont(fontHeader);
            styleHeader.setWrapText(true);
            styleHeader.setBorderTop(BorderStyle.THIN);
            styleHeader.setBorderBottom(BorderStyle.THIN);
            styleHeader.setBorderLeft(BorderStyle.THIN);
            styleHeader.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle styleField = wb.createCellStyle();
            styleField.setAlignment(HorizontalAlignment.LEFT);
            styleField.setVerticalAlignment(VerticalAlignment.TOP);
            styleField.setWrapText(true);
            styleField.setBorderTop(BorderStyle.THIN);
            styleField.setBorderBottom(BorderStyle.THIN);
            styleField.setBorderLeft(BorderStyle.THIN);
            styleField.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle styleVertical = wb.createCellStyle();
            styleVertical.setAlignment(HorizontalAlignment.CENTER);
            styleVertical.setVerticalAlignment(VerticalAlignment.CENTER);
            styleVertical.setRotation((short) 90);
            styleVertical.setFont(fontVertical);
            styleVertical.setFillForegroundColor(new XSSFColor(new byte[]{(byte)0xEA, (byte)0xEA, (byte)0xEA}, null));
            styleVertical.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleVertical.setBorderTop(BorderStyle.THIN);
            styleVertical.setBorderBottom(BorderStyle.THIN);
            styleVertical.setBorderLeft(BorderStyle.THIN);
            styleVertical.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle styleFooter = wb.createCellStyle();
            styleFooter.setAlignment(HorizontalAlignment.CENTER);
            styleFooter.setVerticalAlignment(VerticalAlignment.BOTTOM);
            styleFooter.setFont(fontValue);
            styleFooter.setBorderTop(BorderStyle.THIN);
            styleFooter.setBorderBottom(BorderStyle.THIN);
            styleFooter.setBorderLeft(BorderStyle.THIN);
            styleFooter.setBorderRight(BorderStyle.THIN);

            int rowIdx = 0;

            // ═══════════════════════════════════════════════════════════════
            // 1. CABEÇALHO PRINCIPAL
            // ═══════════════════════════════════════════════════════════════
            Row r0 = sheet.createRow(rowIdx);
            r0.setHeightInPoints(40);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("PREVIDÊNCIA SOCIAL\nCOMUNICAÇÃO DE ACIDENTE DO TRABALHO - CAT");
            c0.setCellStyle(styleHeader);
            for(int i=1; i<=9; i++) r0.createCell(i).setCellStyle(styleHeader);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 9));
            rowIdx++;

            // ═══════════════════════════════════════════════════════════════
            // 2. EMPREGADO
            // ═══════════════════════════════════════════════════════════════
            int startEmpregador = rowIdx;
            createFieldRow(sheet, rowIdx++, new int[]{5, 4}, 
                new String[]{"1 - Emitente", "2 - Tipo de CAT"}, 
                new String[]{"Empregador", "1 - Inicial"}, 
                styleField, fontLabel, fontValue, 25);
            createFieldRow(sheet, rowIdx++, new int[]{5, 4}, 
                new String[]{"3 - Razão Social/Nome", "4 - CNPJ/CEI/CPF/NIT"}, 
                new String[]{"(Empresa preenchida via sistema)", safe(entity.getCnpjEmpresa())}, 
                styleField, fontLabel, fontValue, 25);
            createFieldRow(sheet, rowIdx++, new int[]{4, 3, 2}, 
                new String[]{"5 - Endereço", "6 - Município", "7 - Telefone"}, 
                new String[]{"-", "-", "-"}, 
                styleField, fontLabel, fontValue, 25);
            addVerticalLabel(sheet, startEmpregador, rowIdx - 1, "Empregado", styleVertical);

            // ═══════════════════════════════════════════════════════════════
            // 3. ACIDENTADO
            // ═══════════════════════════════════════════════════════════════
            int startAcidentado = rowIdx;
            createFieldRow(sheet, rowIdx++, new int[]{5, 4}, 
                new String[]{"8 - Nome do Acidentado", "9 - Nome da Mãe"}, 
                new String[]{safe(colab.getNomeCompleto()), "-"}, 
                styleField, fontLabel, fontValue, 25);
            createFieldRow(sheet, rowIdx++, new int[]{2, 2, 2, 3}, 
                new String[]{"10 - Data de Nasc.", "11 - Sexo", "12 - Estado Civil", "13 - CTPS"}, 
                new String[]{colab.getDataNascimento() != null ? colab.getDataNascimento().format(dFmt) : "", "-", "-", "-"}, 
                styleField, fontLabel, fontValue, 25);
            createFieldRow(sheet, rowIdx++, new int[]{3, 2, 4}, 
                new String[]{"14 - Identidade", "15 - PIS/PASEP", "16 - Remuneração Mensal"}, 
                new String[]{"-", safe(colab.getPisPasep()), "-"}, 
                styleField, fontLabel, fontValue, 25);
            createFieldRow(sheet, rowIdx++, new int[]{4, 3, 2}, 
                new String[]{"17 - Endereço", "18 - Município", "19 - Telefone"}, 
                new String[]{"-", "-", safe(colab.getTelefone())}, 
                styleField, fontLabel, fontValue, 25);
            createFieldRow(sheet, rowIdx++, new int[]{4, 5}, 
                new String[]{"20 - Nome da Ocupação", "21 - Setor"}, 
                new String[]{safe(colab.getCargo()), colab.getSetor() != null ? colab.getSetor().name() : ""}, 
                styleField, fontLabel, fontValue, 25);
            addVerticalLabel(sheet, startAcidentado, rowIdx - 1, "Acidentado", styleVertical);

            // ═══════════════════════════════════════════════════════════════
            // 4. ACIDENTE OU DOENÇA
            // ═══════════════════════════════════════════════════════════════
            int startAcidente = rowIdx;
            createFieldRow(sheet, rowIdx++, new int[]{2, 2, 2, 3}, 
                new String[]{"22 - Data do Acidente", "23 - Hora", "24 - Após quantas horas?", "25 - Houve afastamento?"}, 
                new String[]{
                    entity.getDataHora() != null ? entity.getDataHora().format(dFmt) : "", 
                    entity.getDataHora() != null ? entity.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm")) : "", 
                    "-", 
                    (entity.getDiasAfastados() != null && entity.getDiasAfastados() > 0) ? "[ X ] Sim   [   ] Não" : "[   ] Sim   [ X ] Não"
                }, 
                styleField, fontLabel, fontValue, 25);
            createFieldRow(sheet, rowIdx++, new int[]{5, 4}, 
                new String[]{"26 - Local do Acidente", "27 - Especificação do local"}, 
                new String[]{safe(entity.getLocalFabrica()), "-"}, 
                styleField, fontLabel, fontValue, 25);
            createFieldRow(sheet, rowIdx++, new int[]{4, 5}, 
                new String[]{"28 - Parte(s) do corpo atingida(s)", "29 - Agente causador"}, 
                new String[]{safe(entity.getParteCorpoAtingida()), safe(entity.getCausa())}, 
                styleField, fontLabel, fontValue, 25);
            createFieldRow(sheet, rowIdx++, new int[]{9}, 
                new String[]{"30 - Descrição da situação do acidente ou doença"}, 
                new String[]{safe(entity.getDescricao())}, 
                styleField, fontLabel, fontValue, 45);
            createFieldRow(sheet, rowIdx++, new int[]{4, 5}, 
                new String[]{"31 - Houve registro policial?", "32 - Houve morte?"}, 
                new String[]{"[   ] Sim   [ X ] Não", "[   ] Sim   [ X ] Não"}, 
                styleField, fontLabel, fontValue, 25);
            addVerticalLabel(sheet, startAcidente, rowIdx - 1, "Acidente ou\nDoença", styleVertical);

            // ═══════════════════════════════════════════════════════════════
            // 5. TESTEMUNHA
            // ═══════════════════════════════════════════════════════════════
            int startTestemunha = rowIdx;
            createFieldRow(sheet, rowIdx++, new int[]{9}, 
                new String[]{"33 - Nome da(s) Testemunha(s)"}, 
                new String[]{safe(entity.getTestemunhas())}, 
                styleField, fontLabel, fontValue, 30);
            addVerticalLabel(sheet, startTestemunha, rowIdx - 1, "Testemunha", styleVertical);

            // ═══════════════════════════════════════════════════════════════
            // 6. ATENDIMENTO
            // ═══════════════════════════════════════════════════════════════
            int startAtendimento = rowIdx;
            createFieldRow(sheet, rowIdx++, new int[]{5, 2, 2}, 
                new String[]{"34 - Unidade de atendimento médico", "35 - Data", "36 - Hora"}, 
                new String[]{"-", entity.getDataCat() != null ? entity.getDataCat().format(dFmt) : "", "-"}, 
                styleField, fontLabel, fontValue, 25);
            createFieldRow(sheet, rowIdx++, new int[]{3, 3, 3}, 
                new String[]{"37 - Houve internação?", "38 - Duração provável do tratamento", "39 - Deverá o acidentado afastar-se?"}, 
                new String[]{"[   ] Sim   [ X ] Não", entity.getDiasAfastados() != null ? entity.getDiasAfastados() + " dias" : "-", (entity.getDiasAfastados() != null && entity.getDiasAfastados() > 0) ? "[ X ] Sim   [   ] Não" : "[   ] Sim   [ X ] Não"}, 
                styleField, fontLabel, fontValue, 25);
            addVerticalLabel(sheet, startAtendimento, rowIdx - 1, "Atendimento", styleVertical);

            // ═══════════════════════════════════════════════════════════════
            // 7. DIAGNÓSTICO COM LESÃO
            // ═══════════════════════════════════════════════════════════════
            int startDiag = rowIdx;
            createFieldRow(sheet, rowIdx++, new int[]{9}, 
                new String[]{"40 - Descrição e natureza da lesão"}, 
                new String[]{safe(entity.getParteCorpoAtingida())}, 
                styleField, fontLabel, fontValue, 35);
            createFieldRow(sheet, rowIdx++, new int[]{7, 2}, 
                new String[]{"41 - Diagnóstico provável", "42 - CID - 10"}, 
                new String[]{"-", safe(entity.getCid())}, 
                styleField, fontLabel, fontValue, 25);
            createFieldRow(sheet, rowIdx++, new int[]{9}, 
                new String[]{"43 - Observações"}, 
                new String[]{"-"}, 
                styleField, fontLabel, fontValue, 35);
            addVerticalLabel(sheet, startDiag, rowIdx - 1, "Diagnóstico\ncom Lesão", styleVertical);

            // ═══════════════════════════════════════════════════════════════
            // 8. RODAPÉ
            // ═══════════════════════════════════════════════════════════════
            Row rFoot = sheet.createRow(rowIdx);
            rFoot.setHeightInPoints(40);
            
            Cell cFoot1 = rFoot.createCell(0);
            cFoot1.setCellValue("Local e data\n_________________________________");
            cFoot1.setCellStyle(styleFooter);
            for(int i=1; i<=4; i++) rFoot.createCell(i).setCellStyle(styleFooter);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 4));

            Cell cFoot2 = rFoot.createCell(5);
            cFoot2.setCellValue("Assinatura do emitente\n_________________________________");
            cFoot2.setCellStyle(styleFooter);
            for(int i=6; i<=9; i++) rFoot.createCell(i).setCellStyle(styleFooter);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 5, 9));
            rowIdx++;

            Row rLeg = sheet.createRow(rowIdx);
            rLeg.setHeightInPoints(20);
            Cell cLeg = rLeg.createCell(0);
            cLeg.setCellValue("A COMUNICAÇÃO DE ACIDENTE É OBRIGATÓRIA, MESMO NO CASO EM QUE NÃO HAJA AFASTAMENTO DO TRABALHO.");
            
            XSSFCellStyle styleLeg = wb.createCellStyle();
            styleLeg.setAlignment(HorizontalAlignment.CENTER);
            styleLeg.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFFont fontLeg = wb.createFont();
            fontLeg.setFontName("Arial");
            fontLeg.setFontHeightInPoints((short) 8);
            fontLeg.setBold(true);
            styleLeg.setFont(fontLeg);
            styleLeg.setBorderTop(BorderStyle.THIN);
            styleLeg.setBorderBottom(BorderStyle.THIN);
            styleLeg.setBorderLeft(BorderStyle.THIN);
            styleLeg.setBorderRight(BorderStyle.THIN);
            
            cLeg.setCellStyle(styleLeg);
            for(int i=1; i<=9; i++) rLeg.createCell(i).setCellStyle(styleLeg);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, 9));

            wb.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar Excel da CAT", e);
        }
    }

    private void createFieldRow(Sheet sheet, int rowIdx, int[] colSpans, String[] labels, String[] values, XSSFCellStyle style, XSSFFont labelFont, XSSFFont valueFont, int minHeightPts) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);
        
        int maxLines = 1;
        int currentCol = 1;
        for (int i = 0; i < labels.length; i++) {
            int span = colSpans[i];
            String label = labels[i];
            String value = safe(values[i]);
            
            // Estimativa de altura dinâmica para células mescladas:
            // Cada coluna base tem largura 11, o que acomoda aprox 13-14 caracteres com Arial 9.
            int estimatedCharsPerLine = span * 13;
            
            int linesLabel = (int) Math.ceil((double) label.length() / estimatedCharsPerLine);
            if (linesLabel == 0) linesLabel = 1;
            
            int linesValue = 0;
            if (!value.isEmpty()) {
                String[] valueLines = value.split("\n");
                for (String vl : valueLines) {
                    int l = (int) Math.ceil((double) vl.length() / estimatedCharsPerLine);
                    linesValue += (l == 0 ? 1 : l);
                }
            } else {
                linesValue = 1;
            }
            
            int totalLines = linesLabel + linesValue;
            if (totalLines > maxLines) {
                maxLines = totalLines;
            }
            
            Cell cell = row.createCell(currentCol);
            
            XSSFRichTextString rt = new XSSFRichTextString(label + "\n" + value);
            rt.applyFont(0, label.length(), labelFont);
            rt.applyFont(label.length(), rt.length(), valueFont);
            
            cell.setCellValue(rt);
            cell.setCellStyle(style);
            
            for(int c=currentCol+1; c < currentCol + span; c++) {
                Cell c2 = row.createCell(c);
                c2.setCellStyle(style);
            }
            if (span > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, currentCol, currentCol + span - 1));
            }
            currentCol += span;
        }
        
        // Aplica a altura calculada (12 pontos por linha + 8 de margem interna)
        int calculatedHeight = (maxLines * 12) + 8;
        if (calculatedHeight < minHeightPts) {
            calculatedHeight = minHeightPts;
        }
        row.setHeightInPoints(calculatedHeight);
    }

    private void addVerticalLabel(Sheet sheet, int startRow, int endRow, String text, XSSFCellStyle style) {
        for(int r = startRow; r <= endRow; r++) {
            Row row = sheet.getRow(r);
            if(row == null) row = sheet.createRow(r);
            Cell cell = row.createCell(0);
            cell.setCellStyle(style);
        }
        Row firstRow = sheet.getRow(startRow);
        firstRow.getCell(0).setCellValue(text);
        if (endRow > startRow) {
            sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 0, 0));
        }
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
    
    public long countMes() {
        LocalDateTime inicio = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime fim = LocalDate.now().atTime(LocalTime.MAX);
        return repository.countByPeriodo(inicio, fim);
    }
    
    private AcidenteTrabalhoDTO toDTO(AcidenteTrabalho entity) {
        AcidenteTrabalhoDTO dto = new AcidenteTrabalhoDTO();
        dto.setId(entity.getId());
        dto.setColaboradorId(entity.getColaborador().getId());
        dto.setColaboradorNome(entity.getColaborador().getNomeCompleto());
        dto.setColaboradorMatricula(entity.getColaborador().getMatricula());
        dto.setSetor(entity.getColaborador().getSetor().name());
        dto.setDataHora(entity.getDataHora().toString());
        dto.setLocalFabrica(entity.getLocalFabrica());
        dto.setTipo(entity.getTipo());
        dto.setDescricao(entity.getDescricao());
        dto.setCausa(entity.getCausa());
        dto.setMedidasTomadas(entity.getMedidasTomadas());
        dto.setTestemunhas(entity.getTestemunhas());
        dto.setCatEmitida(entity.isCatEmitida());
        dto.setNumeroCat(entity.getNumeroCat());
        dto.setDataCat(entity.getDataCat() != null ? entity.getDataCat().toString() : null);
        dto.setCnpjEmpresa(entity.getCnpjEmpresa());
        dto.setCid(entity.getCid());
        dto.setParteCorpoAtingida(entity.getParteCorpoAtingida());
        dto.setDiasAfastados(entity.getDiasAfastados());
        dto.setPrazoCatVencido(entity.isPrazoCatVencido());
        return dto;
    }
}