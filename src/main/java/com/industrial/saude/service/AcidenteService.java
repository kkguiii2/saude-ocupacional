package com.industrial.saude.service;

import com.industrial.saude.dto.AcidenteTrabalhoDTO;
import com.industrial.saude.model.AcidenteTrabalho;
import com.industrial.saude.model.Colaborador;
import com.industrial.saude.model.Usuario;
import com.industrial.saude.repository.AcidenteTrabalhoRepository;
import com.industrial.saude.repository.ColaboradorRepository;
import com.industrial.saude.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        return gerarPdfCat(entity);
    }

    private byte[] gerarPdfCat(AcidenteTrabalho entity) {
        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            com.lowagie.text.Document document = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(document, out);
            document.open();
            
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font labelFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.NORMAL);
            
            com.lowagie.text.Paragraph title = new com.lowagie.text.Paragraph("COMUNICAÇÃO DE ACIDENTE DE TRABALHO - CAT", titleFont);
            title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            document.add(new com.lowagie.text.Paragraph("Número da CAT: " + entity.getNumeroCat(), labelFont));
            document.add(new com.lowagie.text.Paragraph("Data Emissão: " + (entity.getDataCat() != null ? entity.getDataCat().toString() : ""), dataFont));
            document.add(new com.lowagie.text.Paragraph("CNPJ Empresa: 12.345.678/0001-95", dataFont));
            document.add(new com.lowagie.text.Paragraph("\n"));
            
            document.add(new com.lowagie.text.Paragraph("DADOS DO ACIDENTADO", labelFont));
            document.add(new com.lowagie.text.Paragraph("Nome: " + entity.getColaborador().getNomeCompleto(), dataFont));
            document.add(new com.lowagie.text.Paragraph("Matrícula: " + entity.getColaborador().getMatricula(), dataFont));
            document.add(new com.lowagie.text.Paragraph("Setor: " + entity.getColaborador().getSetor(), dataFont));
            document.add(new com.lowagie.text.Paragraph("PIS/PASEP: " + (entity.getColaborador().getPisPasep() != null ? entity.getColaborador().getPisPasep() : ""), dataFont));
            document.add(new com.lowagie.text.Paragraph("\n"));
            
            document.add(new com.lowagie.text.Paragraph("DADOS DO ACIDENTE", labelFont));
            document.add(new com.lowagie.text.Paragraph("Data/Hora: " + entity.getDataHora().toString(), dataFont));
            document.add(new com.lowagie.text.Paragraph("Local: " + entity.getLocalFabrica(), dataFont));
            document.add(new com.lowagie.text.Paragraph("Tipo: " + entity.getTipo(), dataFont));
            document.add(new com.lowagie.text.Paragraph("Parte do Corpo Atingida: " + (entity.getParteCorpoAtingida() != null ? entity.getParteCorpoAtingida() : ""), dataFont));
            document.add(new com.lowagie.text.Paragraph("CID: " + (entity.getCid() != null ? entity.getCid() : ""), dataFont));
            document.add(new com.lowagie.text.Paragraph("Dias Afastados: " + (entity.getDiasAfastados() != null ? entity.getDiasAfastados() : "0"), dataFont));
            document.add(new com.lowagie.text.Paragraph("\n"));
            
            document.add(new com.lowagie.text.Paragraph("Descrição: " + entity.getDescricao(), dataFont));
            
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF da CAT", e);
        }
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