package com.poo.siga.service;

import com.poo.siga.model.Nota;
import com.poo.siga.repository.NotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotaService {

    private final NotaRepository notaRepository;

    @Transactional(readOnly = true)
    public List<Nota> listarPorTurmaEDisciplinaEPeriodo(Integer turmaId, Integer disciplinaId, Integer periodo) {
        return notaRepository.findByTurmaIdAndDisciplinaIdAndPeriodo(turmaId, disciplinaId, periodo);
    }

    @Transactional(readOnly = true)
    public List<Nota> listarPorTurmaEAluno(Integer turmaId, Integer alunoId) {
        return notaRepository.findByTurmaIdAndAlunoId(turmaId, alunoId);
    }

    @Transactional(readOnly = true)
    public List<Nota> listarPorAluno(Integer alunoId) {
        return notaRepository.findByAlunoId(alunoId);
    }

    @Transactional(readOnly = true)
    public boolean existeNotaParaAluno(Integer alunoId) {
        return notaRepository.existsByAlunoId(alunoId);
    }

    @Transactional
    public Nota registrar(Nota req, String usuarioLogin) {
        // Validações externas omitidas ou poderiam ser chamadas REST via RestTemplate
        req.setDataLancamento(LocalDateTime.now());
        req.setUsuarioLancamento(usuarioLogin);
        return notaRepository.save(req);
    }

    @Transactional
    public Nota atualizar(Integer id, Double novoValor, String usuarioLogin) {
        var nota = notaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nota não encontrada"));
        nota.setValor(Math.round(novoValor * 10.0) / 10.0);
        nota.setDataLancamento(LocalDateTime.now());
        nota.setUsuarioLancamento(usuarioLogin);
        return notaRepository.save(nota);
    }

    public Double calcularMediaBimestral(Integer turmaId, Integer disciplinaId, Integer alunoId, Integer periodo) {
        var notas = notaRepository.buscarNotasDoPeriodo(turmaId, disciplinaId, alunoId, periodo);
        Double av1 = null, av2 = null, rec = null;
        for (var n : notas) {
            switch (n.getTipoAvaliacao()) {
                case AV1 -> av1 = n.getValor();
                case AV2 -> av2 = n.getValor();
                case REC -> rec = n.getValor();
            }
        }
        if (av1 == null || av2 == null) return null;
        double mediaAv = (av1 + av2) / 2.0;
        if (mediaAv >= 5.0) return Math.round(mediaAv * 10.0) / 10.0;
        if (rec != null) {
            return Math.round(Math.max(mediaAv, rec) * 10.0) / 10.0;
        }
        return Math.round(mediaAv * 10.0) / 10.0;
    }

    public Double calcularMediaAnual(Integer turmaId, Integer disciplinaId, Integer alunoId) {
        double soma = 0;
        int count = 0;
        for (int p = 1; p <= 4; p++) {
            Double mb = calcularMediaBimestral(turmaId, disciplinaId, alunoId, p);
            if (mb != null) {
                soma += mb;
                count++;
            }
        }
        if (count < 4) return null;
        return Math.round((soma / 4.0) * 10.0) / 10.0;
    }
}
