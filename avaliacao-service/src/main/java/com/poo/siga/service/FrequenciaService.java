package com.poo.siga.service;

import com.poo.siga.model.Frequencia;
import com.poo.siga.model.enums.StatusFrequencia;
import com.poo.siga.repository.FrequenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FrequenciaService {

    private final FrequenciaRepository frequenciaRepository;

    @Transactional(readOnly = true)
    public List<Frequencia> listarPorTurmaEData(Integer turmaId, Integer disciplinaId, String data) {
        var date = java.time.LocalDate.parse(data);
        return frequenciaRepository.findByTurmaIdAndDisciplinaIdAndData(turmaId, disciplinaId, date);
    }

    @Transactional(readOnly = true)
    public List<Frequencia> listarPorAluno(Integer alunoId) {
        return frequenciaRepository.findByAlunoId(alunoId);
    }

    @Transactional
    public Frequencia registrar(Frequencia frequencia) {
        // Validações externas omitidas para simplicidade do microsserviço ou 
        // devem ser feitas via HTTP RestTemplate API Gateway.
        return frequenciaRepository.save(frequencia);
    }

    @Transactional
    public Frequencia atualizar(Integer id, Frequencia req) {
        var frequencia = frequenciaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Frequência não encontrada"));
        frequencia.setStatus(req.getStatus());
        frequencia.setJustificativa(req.getJustificativa());
        return frequenciaRepository.save(frequencia);
    }

    public Double calcularPercentualPresenca(Integer alunoId, Integer disciplinaId) {
        long total = frequenciaRepository.countByAlunoIdAndDisciplinaId(alunoId, disciplinaId);
        if (total == 0) return null;
        long presencas = frequenciaRepository.countByAlunoIdAndDisciplinaIdAndStatus(
                alunoId, disciplinaId, StatusFrequencia.PRESENTE);
        return Math.round(((double) presencas / total) * 1000.0) / 10.0;
    }
}
