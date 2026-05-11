import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

public class ReplaceScript {
    public static void main(String[] args) throws Exception {
        String path = "src/main/java/com/industrial/saude/service/RelatorioService.java";
        List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        List<String> newLines = new ArrayList<>();
        
        int startIndex = -1;
        int endIndex = -1;
        
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public byte[] gerarExcelColaboradores()")) {
                startIndex = i;
                break;
            }
        }
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("private void addHeader(Document document")) {
                endIndex = i;
                break;
            }
        }
        
        if (startIndex == -1 || endIndex == -1) {
            System.out.println("Error: " + startIndex + ", " + endIndex);
            return;
        }
        
        newLines.addAll(lines.subList(0, startIndex));
        
        String newCode = "    public byte[] gerarExcelColaboradores() {\n" +
"        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();\n" +
"        XSSFWorkbook workbook = new XSSFWorkbook();\n" +
"        Sheet sheet = workbook.createSheet(\"Colaboradores\");\n" +
"        \n" +
"        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);\n" +
"        String[] headers = { \"ID\", \"Nome\", \"Matrícula\", \"Setor\", \"Cargo\", \"Tipo Risco\", \"Status\", \"Data Admissão\", \"EPI's\" };\n" +
"        \n" +
"        int rowNum = createProfessionalHeader(sheet, styles, \"Relatório de Colaboradores\", colaboradores.size(), headers);\n" +
"        \n" +
"        int dataIndex = 0;\n" +
"        for (Colaborador colab : colaboradores) {\n" +
"            Row row = sheet.createRow(rowNum++);\n" +
"            addCell(row, 0, colab.getId(), styles, dataIndex, \"number\");\n" +
"            addCell(row, 1, colab.getNomeCompleto(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 2, colab.getMatricula(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 3, colab.getSetor() != null ? colab.getSetor().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 4, colab.getCargo(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 5, colab.getTipoRisco() != null ? colab.getTipoRisco().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 6, colab.getStatusFuncionario() != null ? colab.getStatusFuncionario().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 7, colab.getDataAdmissao(), styles, dataIndex, \"date\");\n" +
"            addCell(row, 8, colab.getEpisObrigatorios(), styles, dataIndex, \"text\");\n" +
"            dataIndex++;\n" +
"        }\n" +
"        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);\n" +
"        addProfessionalFooter(sheet, styles, rowNum);\n" +
"        \n" +
"        ByteArrayOutputStream baos = new ByteArrayOutputStream();\n" +
"        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error(\"Erro ao gerar Excel: {}\", e.getMessage()); }\n" +
"        return baos.toByteArray();\n" +
"    }\n" +
"\n" +
"    public byte[] gerarExcelAtendimentos() {\n" +
"        List<Atendimento> atendimentos = atendimentoRepository.findAll().stream().filter(Atendimento::isAtivo).toList();\n" +
"        XSSFWorkbook workbook = new XSSFWorkbook();\n" +
"        Sheet sheet = workbook.createSheet(\"Atendimentos\");\n" +
"        \n" +
"        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);\n" +
"        String[] headers = { \"ID\", \"Data/Hora\", \"Colaborador\", \"Matrícula\", \"Setor\", \"Tipo\", \"Gravidade\", \"Emergência\", \"Sintomas\", \"Conduta\", \"Encaminhamento\", \"Profissional\" };\n" +
"        \n" +
"        int rowNum = createProfessionalHeader(sheet, styles, \"Relatório de Atendimentos\", atendimentos.size(), headers);\n" +
"        \n" +
"        int dataIndex = 0;\n" +
"        for (Atendimento at : atendimentos) {\n" +
"            Row row = sheet.createRow(rowNum++);\n" +
"            addCell(row, 0, at.getId(), styles, dataIndex, \"number\");\n" +
"            addCell(row, 1, at.getDataHora(), styles, dataIndex, \"date\");\n" +
"            addCell(row, 2, at.getColaborador() != null ? at.getColaborador().getNomeCompleto() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 3, at.getColaborador() != null ? at.getColaborador().getMatricula() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 4, at.getColaborador() != null && at.getColaborador().getSetor() != null ? at.getColaborador().getSetor().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 5, at.getTipo() != null ? formatTipoAtendimento(at.getTipo()) : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 6, at.getGravidade() != null ? at.getGravidade().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 7, at.isEmergencia(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 8, at.getSintomas(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 9, at.getConduta(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 10, at.getEncaminhamento() != null ? at.getEncaminhamento().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 11, at.getAtendente() != null ? at.getAtendente().getNome() : null, styles, dataIndex, \"text\");\n" +
"            dataIndex++;\n" +
"        }\n" +
"        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);\n" +
"        addProfessionalFooter(sheet, styles, rowNum);\n" +
"        \n" +
"        ByteArrayOutputStream baos = new ByteArrayOutputStream();\n" +
"        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error(\"Erro ao gerar Excel: {}\", e.getMessage()); }\n" +
"        return baos.toByteArray();\n" +
"    }\n" +
"\n" +
"    public byte[] gerarExcelAgendamentos() {\n" +
"        List<Agendamento> agendamentos = agendamentoRepository.findAll();\n" +
"        XSSFWorkbook workbook = new XSSFWorkbook();\n" +
"        Sheet sheet = workbook.createSheet(\"Agendamentos\");\n" +
"        \n" +
"        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);\n" +
"        String[] headers = { \"ID\", \"Data/Hora\", \"Colaborador\", \"Matrícula\", \"Setor\", \"Tipo\", \"Status\", \"Observações\", \"Agendado Por\" };\n" +
"        \n" +
"        int rowNum = createProfessionalHeader(sheet, styles, \"Relatório de Agendamentos\", agendamentos.size(), headers);\n" +
"        \n" +
"        int dataIndex = 0;\n" +
"        for (Agendamento ag : agendamentos) {\n" +
"            Row row = sheet.createRow(rowNum++);\n" +
"            addCell(row, 0, ag.getId(), styles, dataIndex, \"number\");\n" +
"            addCell(row, 1, ag.getDataHora(), styles, dataIndex, \"date\");\n" +
"            addCell(row, 2, ag.getColaborador() != null ? ag.getColaborador().getNomeCompleto() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 3, ag.getColaborador() != null ? ag.getColaborador().getMatricula() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 4, ag.getColaborador() != null && ag.getColaborador().getSetor() != null ? ag.getColaborador().getSetor().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 5, ag.getTipo() != null ? ag.getTipo().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 6, ag.getStatus() != null ? ag.getStatus().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 7, ag.getObservacoes(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 8, ag.getAgendadoPor() != null ? ag.getAgendadoPor().getNome() : null, styles, dataIndex, \"text\");\n" +
"            dataIndex++;\n" +
"        }\n" +
"        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);\n" +
"        addProfessionalFooter(sheet, styles, rowNum);\n" +
"        \n" +
"        ByteArrayOutputStream baos = new ByteArrayOutputStream();\n" +
"        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error(\"Erro ao gerar Excel: {}\", e.getMessage()); }\n" +
"        return baos.toByteArray();\n" +
"    }\n" +
"\n" +
"    public byte[] gerarExcelEstoque() {\n" +
"        List<Medicamento> medicamentos = medicamentoRepository.findAll();\n" +
"        List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAll();\n" +
"        XSSFWorkbook workbook = new XSSFWorkbook();\n" +
"        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);\n" +
"        \n" +
"        Sheet sm = workbook.createSheet(\"Medicamentos\");\n" +
"        String[] h1 = { \"ID\", \"Nome\", \"Princípio Ativo\", \"Categoria\", \"Quantidade\", \"Mínimo\", \"Unidade\", \"Validade\", \"Lote\" };\n" +
"        int rn = createProfessionalHeader(sm, styles, \"Relatório de Estoque - Medicamentos\", medicamentos.size(), h1);\n" +
"        int dataIndex = 0;\n" +
"        for (Medicamento med : medicamentos) {\n" +
"            Row row = sm.createRow(rn++);\n" +
"            addCell(row, 0, med.getId(), styles, dataIndex, \"number\");\n" +
"            addCell(row, 1, med.getNome(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 2, med.getPrincipioAtivo(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 3, med.getCategoria() != null ? med.getCategoria().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 4, med.getQuantidadeEstoque(), styles, dataIndex, \"number\");\n" +
"            addCell(row, 5, med.getQuantidadeMinima() != null ? med.getQuantidadeMinima() : 0, styles, dataIndex, \"number\");\n" +
"            addCell(row, 6, med.getUnidade(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 7, med.getDataValidade(), styles, dataIndex, \"date\");\n" +
"            addCell(row, 8, med.getLote(), styles, dataIndex, \"text\");\n" +
"            dataIndex++;\n" +
"        }\n" +
"        for (int i = 0; i < h1.length; i++) sm.autoSizeColumn(i);\n" +
"        addProfessionalFooter(sm, styles, rn);\n" +
"\n" +
"        Sheet sv = workbook.createSheet(\"Movimentações\");\n" +
"        String[] h2 = { \"ID\", \"Data\", \"Tipo\", \"Medicamento\", \"Quantidade\", \"Descrição\", \"Usuário\" };\n" +
"        rn = createProfessionalHeader(sv, styles, \"Relatório de Estoque - Movimentações\", movimentacoes.size(), h2);\n" +
"        dataIndex = 0;\n" +
"        for (MovimentacaoEstoque mov : movimentacoes) {\n" +
"            Row row = sv.createRow(rn++);\n" +
"            addCell(row, 0, mov.getId(), styles, dataIndex, \"number\");\n" +
"            addCell(row, 1, mov.getDataHora(), styles, dataIndex, \"date\");\n" +
"            addCell(row, 2, mov.getTipo() != null ? mov.getTipo().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 3, mov.getMedicamento() != null ? mov.getMedicamento().getNome() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 4, mov.getQuantidade(), styles, dataIndex, \"number\");\n" +
"            addCell(row, 5, mov.getMotivo(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 6, mov.getResponsavel() != null ? mov.getResponsavel().getNome() : null, styles, dataIndex, \"text\");\n" +
"            dataIndex++;\n" +
"        }\n" +
"        for (int i = 0; i < h2.length; i++) sv.autoSizeColumn(i);\n" +
"        addProfessionalFooter(sv, styles, rn);\n" +
"\n" +
"        ByteArrayOutputStream baos = new ByteArrayOutputStream();\n" +
"        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error(\"Erro ao gerar Excel: {}\", e.getMessage()); }\n" +
"        return baos.toByteArray();\n" +
"    }\n" +
"\n" +
"    public byte[] gerarExcelAcidentes() {\n" +
"        List<AcidenteTrabalho> acidentes = acidenteTrabalhoRepository.findAll();\n" +
"        XSSFWorkbook workbook = new XSSFWorkbook();\n" +
"        Sheet sheet = workbook.createSheet(\"Acidentes de Trabalho\");\n" +
"        \n" +
"        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);\n" +
"        String[] headers = { \"ID\", \"Data/Hora\", \"Colaborador\", \"Matrícula\", \"Setor\", \"Tipo\", \"Local\", \"Descrição\", \"Causa\", \"CAT Emitida\", \"Nº CAT\", \"Registrado Por\" };\n" +
"        \n" +
"        int rowNum = createProfessionalHeader(sheet, styles, \"Relatório de Acidentes de Trabalho\", acidentes.size(), headers);\n" +
"        \n" +
"        int dataIndex = 0;\n" +
"        for (AcidenteTrabalho ac : acidentes) {\n" +
"            Row row = sheet.createRow(rowNum++);\n" +
"            addCell(row, 0, ac.getId(), styles, dataIndex, \"number\");\n" +
"            addCell(row, 1, ac.getDataHora(), styles, dataIndex, \"date\");\n" +
"            addCell(row, 2, ac.getColaborador() != null ? ac.getColaborador().getNomeCompleto() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 3, ac.getColaborador() != null ? ac.getColaborador().getMatricula() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 4, ac.getColaborador() != null && ac.getColaborador().getSetor() != null ? ac.getColaborador().getSetor().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 5, ac.getTipo() != null ? ac.getTipo().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 6, ac.getLocalFabrica(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 7, ac.getDescricao(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 8, ac.getCausa(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 9, ac.isCatEmitida(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 10, ac.getNumeroCat(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 11, ac.getRegistradoPor() != null ? ac.getRegistradoPor().getNome() : null, styles, dataIndex, \"text\");\n" +
"            dataIndex++;\n" +
"        }\n" +
"        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);\n" +
"        addProfessionalFooter(sheet, styles, rowNum);\n" +
"        \n" +
"        ByteArrayOutputStream baos = new ByteArrayOutputStream();\n" +
"        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error(\"Erro ao gerar Excel de acidentes: {}\", e.getMessage()); }\n" +
"        return baos.toByteArray();\n" +
"    }\n" +
"\n" +
"    public byte[] gerarExcelCompleto(List<String> modulos) {\n" +
"        XSSFWorkbook workbook = new XSSFWorkbook();\n" +
"        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);\n" +
"\n" +
"        if (modulos.contains(\"colaboradores\")) {\n" +
"            List<Colaborador> colaboradores = colaboradorRepository.findAll();\n" +
"            Sheet sheet = workbook.createSheet(\"Colaboradores\");\n" +
"            String[] h = { \"ID\", \"Nome\", \"Matrícula\", \"Setor\", \"Cargo\", \"Tipo Risco\", \"Status\", \"Data Admissão\", \"EPI's\" };\n" +
"            int rn = createProfessionalHeader(sheet, styles, \"Relatório de Colaboradores\", colaboradores.size(), h);\n" +
"            int dataIndex = 0;\n" +
"            for (Colaborador c : colaboradores) {\n" +
"                Row row = sheet.createRow(rn++);\n" +
"                addCell(row, 0, c.getId(), styles, dataIndex, \"number\");\n" +
"                addCell(row, 1, c.getNomeCompleto(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 2, c.getMatricula(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 3, c.getSetor() != null ? c.getSetor().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 4, c.getCargo(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 5, c.getTipoRisco() != null ? c.getTipoRisco().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 6, c.getStatusFuncionario() != null ? c.getStatusFuncionario().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 7, c.getDataAdmissao(), styles, dataIndex, \"date\");\n" +
"                addCell(row, 8, c.getEpisObrigatorios(), styles, dataIndex, \"text\");\n" +
"                dataIndex++;\n" +
"            }\n" +
"            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);\n" +
"            addProfessionalFooter(sheet, styles, rn);\n" +
"        }\n" +
"\n" +
"        if (modulos.contains(\"atendimentos\")) {\n" +
"            List<Atendimento> atendimentos = atendimentoRepository.findAll().stream().filter(Atendimento::isAtivo).toList();\n" +
"            Sheet sheet = workbook.createSheet(\"Atendimentos\");\n" +
"            String[] h = { \"ID\", \"Data/Hora\", \"Colaborador\", \"Matrícula\", \"Setor\", \"Tipo\", \"Gravidade\", \"Emergência\", \"Sintomas\", \"Conduta\", \"Encaminhamento\", \"Profissional\" };\n" +
"            int rn = createProfessionalHeader(sheet, styles, \"Relatório de Atendimentos\", atendimentos.size(), h);\n" +
"            int dataIndex = 0;\n" +
"            for (Atendimento at : atendimentos) {\n" +
"                Row row = sheet.createRow(rn++);\n" +
"                addCell(row, 0, at.getId(), styles, dataIndex, \"number\");\n" +
"                addCell(row, 1, at.getDataHora(), styles, dataIndex, \"date\");\n" +
"                addCell(row, 2, at.getColaborador() != null ? at.getColaborador().getNomeCompleto() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 3, at.getColaborador() != null ? at.getColaborador().getMatricula() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 4, at.getColaborador() != null && at.getColaborador().getSetor() != null ? at.getColaborador().getSetor().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 5, at.getTipo() != null ? formatTipoAtendimento(at.getTipo()) : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 6, at.getGravidade() != null ? at.getGravidade().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 7, at.isEmergencia(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 8, at.getSintomas(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 9, at.getConduta(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 10, at.getEncaminhamento() != null ? at.getEncaminhamento().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 11, at.getAtendente() != null ? at.getAtendente().getNome() : null, styles, dataIndex, \"text\");\n" +
"                dataIndex++;\n" +
"            }\n" +
"            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);\n" +
"            addProfessionalFooter(sheet, styles, rn);\n" +
"        }\n" +
"\n" +
"        if (modulos.contains(\"agendamentos\")) {\n" +
"            List<Agendamento> agendamentos = agendamentoRepository.findAll();\n" +
"            Sheet sheet = workbook.createSheet(\"Agendamentos\");\n" +
"            String[] h = { \"ID\", \"Data/Hora\", \"Colaborador\", \"Matrícula\", \"Setor\", \"Tipo\", \"Status\", \"Observações\", \"Agendado Por\" };\n" +
"            int rn = createProfessionalHeader(sheet, styles, \"Relatório de Agendamentos\", agendamentos.size(), h);\n" +
"            int dataIndex = 0;\n" +
"            for (Agendamento ag : agendamentos) {\n" +
"                Row row = sheet.createRow(rn++);\n" +
"                addCell(row, 0, ag.getId(), styles, dataIndex, \"number\");\n" +
"                addCell(row, 1, ag.getDataHora(), styles, dataIndex, \"date\");\n" +
"                addCell(row, 2, ag.getColaborador() != null ? ag.getColaborador().getNomeCompleto() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 3, ag.getColaborador() != null ? ag.getColaborador().getMatricula() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 4, ag.getColaborador() != null && ag.getColaborador().getSetor() != null ? ag.getColaborador().getSetor().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 5, ag.getTipo() != null ? ag.getTipo().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 6, ag.getStatus() != null ? ag.getStatus().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 7, ag.getObservacoes(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 8, ag.getAgendadoPor() != null ? ag.getAgendadoPor().getNome() : null, styles, dataIndex, \"text\");\n" +
"                dataIndex++;\n" +
"            }\n" +
"            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);\n" +
"            addProfessionalFooter(sheet, styles, rn);\n" +
"        }\n" +
"\n" +
"        if (modulos.contains(\"acidentes\")) {\n" +
"            List<AcidenteTrabalho> acidentes = acidenteTrabalhoRepository.findAll();\n" +
"            Sheet sheet = workbook.createSheet(\"Acidentes de Trabalho\");\n" +
"            String[] h = { \"ID\", \"Data/Hora\", \"Colaborador\", \"Matrícula\", \"Setor\", \"Tipo\", \"Local\", \"Descrição\", \"Causa\", \"CAT Emitida\", \"Nº CAT\", \"Registrado Por\" };\n" +
"            int rn = createProfessionalHeader(sheet, styles, \"Relatório de Acidentes de Trabalho\", acidentes.size(), h);\n" +
"            int dataIndex = 0;\n" +
"            for (AcidenteTrabalho ac : acidentes) {\n" +
"                Row row = sheet.createRow(rn++);\n" +
"                addCell(row, 0, ac.getId(), styles, dataIndex, \"number\");\n" +
"                addCell(row, 1, ac.getDataHora(), styles, dataIndex, \"date\");\n" +
"                addCell(row, 2, ac.getColaborador() != null ? ac.getColaborador().getNomeCompleto() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 3, ac.getColaborador() != null ? ac.getColaborador().getMatricula() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 4, ac.getColaborador() != null && ac.getColaborador().getSetor() != null ? ac.getColaborador().getSetor().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 5, ac.getTipo() != null ? ac.getTipo().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 6, ac.getLocalFabrica(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 7, ac.getDescricao(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 8, ac.getCausa(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 9, ac.isCatEmitida(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 10, ac.getNumeroCat(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 11, ac.getRegistradoPor() != null ? ac.getRegistradoPor().getNome() : null, styles, dataIndex, \"text\");\n" +
"                dataIndex++;\n" +
"            }\n" +
"            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);\n" +
"            addProfessionalFooter(sheet, styles, rn);\n" +
"        }\n" +
"\n" +
"        if (modulos.contains(\"medicamentos\")) {\n" +
"            List<Medicamento> medicamentos = medicamentoRepository.findAll();\n" +
"            Sheet sheet = workbook.createSheet(\"Medicamentos\");\n" +
"            String[] h = { \"ID\", \"Nome\", \"Princípio Ativo\", \"Categoria\", \"Quantidade\", \"Mínimo\", \"Unidade\", \"Validade\", \"Lote\" };\n" +
"            int rn = createProfessionalHeader(sheet, styles, \"Relatório de Estoque - Medicamentos\", medicamentos.size(), h);\n" +
"            int dataIndex = 0;\n" +
"            for (Medicamento med : medicamentos) {\n" +
"                Row row = sheet.createRow(rn++);\n" +
"                addCell(row, 0, med.getId(), styles, dataIndex, \"number\");\n" +
"                addCell(row, 1, med.getNome(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 2, med.getPrincipioAtivo(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 3, med.getCategoria() != null ? med.getCategoria().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 4, med.getQuantidadeEstoque(), styles, dataIndex, \"number\");\n" +
"                addCell(row, 5, med.getQuantidadeMinima() != null ? med.getQuantidadeMinima() : 0, styles, dataIndex, \"number\");\n" +
"                addCell(row, 6, med.getUnidade(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 7, med.getDataValidade(), styles, dataIndex, \"date\");\n" +
"                addCell(row, 8, med.getLote(), styles, dataIndex, \"text\");\n" +
"                dataIndex++;\n" +
"            }\n" +
"            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);\n" +
"            addProfessionalFooter(sheet, styles, rn);\n" +
"        }\n" +
"\n" +
"        if (modulos.contains(\"movimentacoes\")) {\n" +
"            List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAll();\n" +
"            Sheet sheet = workbook.createSheet(\"Movimentações Estoque\");\n" +
"            String[] h = { \"ID\", \"Data\", \"Tipo\", \"Medicamento\", \"Quantidade\", \"Motivo\", \"Responsável\" };\n" +
"            int rn = createProfessionalHeader(sheet, styles, \"Relatório de Estoque - Movimentações\", movimentacoes.size(), h);\n" +
"            int dataIndex = 0;\n" +
"            for (MovimentacaoEstoque mov : movimentacoes) {\n" +
"                Row row = sheet.createRow(rn++);\n" +
"                addCell(row, 0, mov.getId(), styles, dataIndex, \"number\");\n" +
"                addCell(row, 1, mov.getDataHora(), styles, dataIndex, \"date\");\n" +
"                addCell(row, 2, mov.getTipo() != null ? mov.getTipo().name() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 3, mov.getMedicamento() != null ? mov.getMedicamento().getNome() : null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 4, mov.getQuantidade(), styles, dataIndex, \"number\");\n" +
"                addCell(row, 5, mov.getMotivo(), styles, dataIndex, \"text\");\n" +
"                addCell(row, 6, mov.getResponsavel() != null ? mov.getResponsavel().getNome() : null, styles, dataIndex, \"text\");\n" +
"                dataIndex++;\n" +
"            }\n" +
"            for (int i = 0; i < h.length; i++) sheet.autoSizeColumn(i);\n" +
"            addProfessionalFooter(sheet, styles, rn);\n" +
"        }\n" +
"\n" +
"        ByteArrayOutputStream baos = new ByteArrayOutputStream();\n" +
"        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error(\"Erro ao gerar Excel completo: {}\", e.getMessage()); }\n" +
"        return baos.toByteArray();\n" +
"    }\n" +
"\n" +
"    public byte[] gerarExcelNR7() {\n" +
"        List<Colaborador> colaboradores = colaboradorRepository.findByAtivoTrue();\n" +
"        XSSFWorkbook workbook = new XSSFWorkbook();\n" +
"        Sheet sheet = workbook.createSheet(\"Relatório PCMSO (NR-7 e eSocial)\");\n" +
"        \n" +
"        ExcelProfessionalStyles styles = new ExcelProfessionalStyles(workbook);\n" +
"        String[] headers = {\n" +
"            \"ID\", \"Nome do Colaborador\", \"Matrícula\", \"PIS/PASEP\", \"Setor\", \"Cargo\",\n" +
"            \"Data Admissão\", \"Risco Ocupacional\", \"Último Exame (ASO)\", \"Próximo Exame\",\n" +
"            \"Restrições\", \"Observações Médicas\"\n" +
"        };\n" +
"        \n" +
"        int rowNum = createProfessionalHeader(sheet, styles, \"Relatório PCMSO (NR-7 e eSocial)\", colaboradores.size(), headers);\n" +
"        \n" +
"        int dataIndex = 0;\n" +
"        for (Colaborador colab : colaboradores) {\n" +
"            Row row = sheet.createRow(rowNum++);\n" +
"            addCell(row, 0, colab.getId(), styles, dataIndex, \"number\");\n" +
"            addCell(row, 1, colab.getNomeCompleto(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 2, colab.getMatricula(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 3, colab.getPisPasep(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 4, colab.getSetor() != null ? colab.getSetor().name() : null, styles, dataIndex, \"text\");\n" +
"            addCell(row, 5, colab.getCargo(), styles, dataIndex, \"text\");\n" +
"            addCell(row, 6, colab.getDataAdmissao(), styles, dataIndex, \"date\");\n" +
"            addCell(row, 7, colab.getTipoRisco() != null ? colab.getTipoRisco().name() : null, styles, dataIndex, \"text\");\n" +
"            \n" +
"            ProntuarioOcupacional p = colab.getProntuario();\n" +
"            if (p != null) {\n" +
"                addCell(row, 8, p.getUltimoExame(), styles, dataIndex, \"date\");\n" +
"                addCell(row, 9, p.getProximoExame(), styles, dataIndex, \"date\");\n" +
"                addCell(row, 10, p.getRestricoesTrabalho(), styles, dataIndex, \"text\");\n" +
"                String obs = \"\";\n" +
"                if (p.getAlergias() != null) obs += \"Alergias: \" + p.getAlergias() + \" \";\n" +
"                if (p.getMedicacoesUso() != null) obs += \"Med: \" + p.getMedicacoesUso();\n" +
"                addCell(row, 11, obs.trim(), styles, dataIndex, \"text\");\n" +
"            } else {\n" +
"                addCell(row, 8, null, styles, dataIndex, \"date\");\n" +
"                addCell(row, 9, null, styles, dataIndex, \"date\");\n" +
"                addCell(row, 10, null, styles, dataIndex, \"text\");\n" +
"                addCell(row, 11, \"Sem Prontuário\", styles, dataIndex, \"text\");\n" +
"            }\n" +
"            dataIndex++;\n" +
"        }\n" +
"        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);\n" +
"        addProfessionalFooter(sheet, styles, rowNum);\n" +
"        \n" +
"        ByteArrayOutputStream baos = new ByteArrayOutputStream();\n" +
"        try { workbook.write(baos); workbook.close(); } catch (IOException e) { log.error(\"Erro ao gerar Excel NR-7: {}\", e.getMessage()); }\n" +
"        return baos.toByteArray();\n" +
"    }\n" +
"\n" +
"    private static class ExcelProfessionalStyles {\n" +
"        final XSSFCellStyle title;\n" +
"        final XSSFCellStyle info;\n" +
"        final XSSFCellStyle header;\n" +
"        final XSSFCellStyle rowEvenLeft;\n" +
"        final XSSFCellStyle rowOddLeft;\n" +
"        final XSSFCellStyle rowEvenRight;\n" +
"        final XSSFCellStyle rowOddRight;\n" +
"        final XSSFCellStyle rowEvenCenter;\n" +
"        final XSSFCellStyle rowOddCenter;\n" +
"        final XSSFCellStyle footer;\n" +
"\n" +
"        ExcelProfessionalStyles(XSSFWorkbook wb) {\n" +
"            title = wb.createCellStyle();\n" +
"            XSSFFont titleFont = wb.createFont();\n" +
"            titleFont.setFontName(\"Arial\");\n" +
"            titleFont.setFontHeightInPoints((short) 14);\n" +
"            titleFont.setBold(true);\n" +
"            title.setFont(titleFont);\n" +
"            title.setAlignment(HorizontalAlignment.CENTER);\n" +
"\n" +
"            info = wb.createCellStyle();\n" +
"            XSSFFont infoFont = wb.createFont();\n" +
"            infoFont.setFontName(\"Arial\");\n" +
"            infoFont.setFontHeightInPoints((short) 10);\n" +
"            info.setFont(infoFont);\n" +
"            info.setAlignment(HorizontalAlignment.LEFT);\n" +
"\n" +
"            header = wb.createCellStyle();\n" +
"            XSSFFont headerFont = wb.createFont();\n" +
"            headerFont.setFontName(\"Arial\");\n" +
"            headerFont.setFontHeightInPoints((short) 10);\n" +
"            headerFont.setBold(true);\n" +
"            headerFont.setColor(IndexedColors.WHITE.getIndex());\n" +
"            header.setFont(headerFont);\n" +
"            header.setFillForegroundColor(new XSSFColor(new byte[]{(byte)0x40, (byte)0x40, (byte)0x40}, null));\n" +
"            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);\n" +
"            header.setAlignment(HorizontalAlignment.CENTER);\n" +
"            header.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);\n" +
"            applyBorder(header);\n" +
"\n" +
"            XSSFFont dataFont = wb.createFont();\n" +
"            dataFont.setFontName(\"Arial\");\n" +
"            dataFont.setFontHeightInPoints((short) 10);\n" +
"\n" +
"            XSSFColor colorWhite = new XSSFColor(new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF}, null);\n" +
"            XSSFColor colorZebra = new XSSFColor(new byte[]{(byte)0xF5, (byte)0xF5, (byte)0xF5}, null);\n" +
"\n" +
"            rowEvenLeft = createDataStyle(wb, dataFont, colorWhite, HorizontalAlignment.LEFT);\n" +
"            rowOddLeft = createDataStyle(wb, dataFont, colorZebra, HorizontalAlignment.LEFT);\n" +
"\n" +
"            rowEvenRight = createDataStyle(wb, dataFont, colorWhite, HorizontalAlignment.RIGHT);\n" +
"            rowOddRight = createDataStyle(wb, dataFont, colorZebra, HorizontalAlignment.RIGHT);\n" +
"\n" +
"            rowEvenCenter = createDataStyle(wb, dataFont, colorWhite, HorizontalAlignment.CENTER);\n" +
"            rowOddCenter = createDataStyle(wb, dataFont, colorZebra, HorizontalAlignment.CENTER);\n" +
"\n" +
"            footer = wb.createCellStyle();\n" +
"            XSSFFont footerFont = wb.createFont();\n" +
"            footerFont.setFontName(\"Arial\");\n" +
"            footerFont.setFontHeightInPoints((short) 9);\n" +
"            footerFont.setItalic(true);\n" +
"            footer.setFont(footerFont);\n" +
"            footer.setAlignment(HorizontalAlignment.LEFT);\n" +
"        }\n" +
"\n" +
"        private XSSFCellStyle createDataStyle(XSSFWorkbook wb, XSSFFont font, XSSFColor bgColor, HorizontalAlignment align) {\n" +
"            XSSFCellStyle style = wb.createCellStyle();\n" +
"            style.setFont(font);\n" +
"            style.setFillForegroundColor(bgColor);\n" +
"            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);\n" +
"            style.setAlignment(align);\n" +
"            style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);\n" +
"            style.setWrapText(true);\n" +
"            applyBorder(style);\n" +
"            return style;\n" +
"        }\n" +
"\n" +
"        private void applyBorder(XSSFCellStyle style) {\n" +
"            style.setBorderTop(BorderStyle.THIN);\n" +
"            style.setBorderBottom(BorderStyle.THIN);\n" +
"            style.setBorderLeft(BorderStyle.THIN);\n" +
"            style.setBorderRight(BorderStyle.THIN);\n" +
"        }\n" +
"    }\n" +
"\n" +
"    private int createProfessionalHeader(Sheet sheet, ExcelProfessionalStyles styles, String titleText, int totalRegistros, String[] headers) {\n" +
"        Row titleRow = sheet.createRow(0);\n" +
"        Cell titleCell = titleRow.createCell(0);\n" +
"        titleCell.setCellValue(titleText);\n" +
"        titleCell.setCellStyle(styles.title);\n" +
"        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));\n" +
"        \n" +
"        Row infoRow1 = sheet.createRow(2);\n" +
"        Cell infoCell1 = infoRow1.createCell(0);\n" +
"        infoCell1.setCellValue(\"Data de geração: \" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(\"dd/MM/yyyy HH:mm\")));\n" +
"        infoCell1.setCellStyle(styles.info);\n" +
"\n" +
"        Row infoRow2 = sheet.createRow(3);\n" +
"        Cell infoCell2 = infoRow2.createCell(0);\n" +
"        infoCell2.setCellValue(\"Total de registros: \" + totalRegistros);\n" +
"        infoCell2.setCellStyle(styles.info);\n" +
"\n" +
"        Row headerRow = sheet.createRow(5);\n" +
"        for (int i = 0; i < headers.length; i++) {\n" +
"            Cell cell = headerRow.createCell(i);\n" +
"            cell.setCellValue(headers[i]);\n" +
"            cell.setCellStyle(styles.header);\n" +
"        }\n" +
"        sheet.setAutoFilter(new CellRangeAddress(5, 5, 0, headers.length - 1));\n" +
"\n" +
"        return 6; \n" +
"    }\n" +
"\n" +
"    private void addProfessionalFooter(Sheet sheet, ExcelProfessionalStyles styles, int currentRow) {\n" +
"        Row footerRow = sheet.createRow(currentRow + 1);\n" +
"        Cell footerCell = footerRow.createCell(0);\n" +
"        footerCell.setCellValue(\"Relatório gerado automaticamente pelo sistema de Saúde Ocupacional\");\n" +
"        footerCell.setCellStyle(styles.footer);\n" +
"    }\n" +
"\n" +
"    private void addCell(Row row, int col, Object value, ExcelProfessionalStyles styles, int dataIndex, String formatType) {\n" +
"        Cell cell = row.createCell(col);\n" +
"        boolean isEven = (dataIndex % 2 == 0);\n" +
"        \n" +
"        String strValue = \"-\";\n" +
"        \n" +
"        if (value != null) {\n" +
"            if (value instanceof String) {\n" +
"                if (!((String) value).trim().isEmpty()) {\n" +
"                    strValue = (String) value;\n" +
"                }\n" +
"            } else if (value instanceof Boolean) {\n" +
"                strValue = (Boolean) value ? \"Sim\" : \"Não\";\n" +
"            } else if (value instanceof LocalDateTime) {\n" +
"                strValue = ((LocalDateTime) value).format(DateTimeFormatter.ofPattern(\"dd/MM/yyyy\"));\n" +
"            } else if (value instanceof java.time.LocalDate) {\n" +
"                strValue = ((java.time.LocalDate) value).format(DateTimeFormatter.ofPattern(\"dd/MM/yyyy\"));\n" +
"            } else if (value instanceof Enum) {\n" +
"                strValue = ((Enum<?>) value).name();\n" +
"            } else {\n" +
"                strValue = value.toString();\n" +
"                if (strValue.trim().isEmpty()) {\n" +
"                    strValue = \"-\";\n" +
"                }\n" +
"            }\n" +
"        }\n" +
"        \n" +
"        if (\"currency\".equals(formatType) && value instanceof Number) {\n" +
"            strValue = String.format(\"R$ %,.2f\", ((Number) value).doubleValue());\n" +
"        }\n" +
"        \n" +
"        if (strValue.trim().isEmpty()) {\n" +
"            strValue = \"-\";\n" +
"        }\n" +
"        cell.setCellValue(strValue);\n" +
"\n" +
"        if (\"number\".equals(formatType) || \"currency\".equals(formatType) || value instanceof Number) {\n" +
"            cell.setCellStyle(isEven ? styles.rowEvenRight : styles.rowOddRight);\n" +
"        } else if (\"date\".equals(formatType) || value instanceof LocalDateTime || value instanceof java.time.LocalDate) {\n" +
"            cell.setCellStyle(isEven ? styles.rowEvenCenter : styles.rowOddCenter);\n" +
"        } else {\n" +
"            cell.setCellStyle(isEven ? styles.rowEvenLeft : styles.rowOddLeft);\n" +
"        }\n" +
"    }\n";

        newLines.add(newCode);
        newLines.addAll(lines.subList(endIndex, lines.size()));
        
        Files.write(Paths.get(path), newLines, StandardCharsets.UTF_8);
        System.out.println("Done.");
    }
}
