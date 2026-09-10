package com.poo.siga.service;

import com.poo.siga.dto.matricula.*;
import com.poo.siga.model.Matricula;
import com.poo.siga.model.enums.SituacaoMatricula;
import com.poo.siga.model.enums.Turno;
import com.poo.siga.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final RestTemplate restTemplate;

    @Value("${gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    // ─── Leitura ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<MatriculaResponse> listar(String nomeAluno, Integer turmaId, Integer anoLetivo,
                                           SituacaoMatricula situacao, Pageable pageable) {
        return matriculaRepository.buscarComFiltros(nomeAluno, turmaId, anoLetivo, situacao, pageable)
                .map(MatriculaResponse::from);
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponse> listarPorTurma(Integer turmaId) {
        return matriculaRepository.findByTurmaId(turmaId).stream()
                .map(MatriculaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponse> listarPorAluno(Integer alunoId) {
        return matriculaRepository.findByAlunoId(alunoId).stream()
                .map(MatriculaResponse::from).toList();
    }

    /** Usado pelo aluno-service para verificar se pode excluir um aluno. */
    @Transactional(readOnly = true)
    public boolean existeMatriculaParaAluno(Integer alunoId) {
        return matriculaRepository.existsByAlunoId(alunoId);
    }

    // ─── Escrita ─────────────────────────────────────────────────────────────

    @Transactional
    public MatriculaResponse criar(MatriculaRequest req) {
        // 1. Busca dados do Aluno via API Gateway → Aluno Service
        AlunoInfo aluno = buscarAluno(req.alunoId());
        if (!aluno.ativo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aluno inativo não pode ser matriculado");
        }

        // 2. Busca dados da Turma via API Gateway → Acadêmico Service
        TurmaInfo turma = buscarTurma(req.turmaId());

        // 3. Regra de negócio: apenas uma matrícula ATIVA por ano letivo
        if (matriculaRepository.existsByAlunoIdAndAnoLetivoAndSituacao(
                aluno.id(), turma.anoLetivo(), SituacaoMatricula.ATIVA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Aluno já possui matrícula ativa neste ano letivo");
        }

        Matricula m = new Matricula();
        m.setNumero(gerarNumero(turma.anoLetivo()));
        m.setAlunoId(aluno.id());
        m.setAlunoNome(aluno.nome());
        m.setAlunoRa(aluno.ra());
        m.setTurmaId(turma.id());
        m.setTurmaIdentificacao(turma.identificacao());
        m.setAnoLetivo(turma.anoLetivo());
        m.setSerie(turma.serie());
        m.setTurno(Turno.valueOf(turma.turno()));
        m.setDataMatricula(LocalDate.now());
        m.setSituacao(SituacaoMatricula.ATIVA);
        return MatriculaResponse.from(matriculaRepository.save(m));
    }

    @Transactional
    public MatriculaResponse trancar(Integer id, String motivo) {
        Matricula m = findOrThrow(id);
        if (m.getSituacao() != SituacaoMatricula.ATIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Somente matrículas ativas podem ser trancadas");
        }
        m.setSituacao(SituacaoMatricula.TRANCADA);
        m.setMotivoCancelamento(motivo);
        m.setDataCancelamento(LocalDate.now());
        return MatriculaResponse.from(matriculaRepository.save(m));
    }

    @Transactional
    public MatriculaResponse cancelar(Integer id, String motivo) {
        Matricula m = findOrThrow(id);
        if (m.getSituacao() == SituacaoMatricula.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula já cancelada");
        }
        m.setSituacao(SituacaoMatricula.CANCELADA);
        m.setMotivoCancelamento(motivo);
        m.setDataCancelamento(LocalDate.now());
        return MatriculaResponse.from(matriculaRepository.save(m));
    }

    @Transactional
    public MatriculaResponse concluir(Integer id) {
        Matricula m = findOrThrow(id);
        if (m.getSituacao() != SituacaoMatricula.ATIVA && m.getSituacao() != SituacaoMatricula.TRANCADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Somente matrículas ativas ou trancadas podem ser concluídas");
        }
        m.setSituacao(SituacaoMatricula.CONCLUIDA);
        m.setDataCancelamento(LocalDate.now());
        return MatriculaResponse.from(matriculaRepository.save(m));
    }

    @Transactional
    public MatriculaResponse reativar(Integer id) {
        Matricula m = findOrThrow(id);
        if (m.getSituacao() != SituacaoMatricula.CONCLUIDA && m.getSituacao() != SituacaoMatricula.TRANCADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Somente matrículas concluídas ou trancadas podem ser reativadas");
        }
        if (matriculaRepository.existsByAlunoIdAndAnoLetivoAndSituacao(
                m.getAlunoId(), m.getAnoLetivo(), SituacaoMatricula.ATIVA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Aluno já possui outra matrícula ativa neste ano letivo");
        }
        m.setSituacao(SituacaoMatricula.ATIVA);
        m.setMotivoCancelamento(null);
        m.setDataCancelamento(null);
        return MatriculaResponse.from(matriculaRepository.save(m));
    }

    @Transactional
    public RematriculaResult rematricular(RematriculaRequest req) {
        if (req.turmaOrigemId().equals(req.turmaDestinoId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A turma de origem e destino não podem ser iguais");
        }

        TurmaInfo turmaDestino = buscarTurma(req.turmaDestinoId());

        List<Matricula> matriculasOrigem = matriculaRepository.findByTurmaId(req.turmaOrigemId())
                .stream()
                .filter(m -> m.getSituacao() == SituacaoMatricula.ATIVA
                          || m.getSituacao() == SituacaoMatricula.CONCLUIDA)
                .toList();

        int total = matriculasOrigem.size();
        int matriculados = 0;
        List<String> ignorados = new ArrayList<>();

        for (Matricula origem : matriculasOrigem) {
            if (matriculaRepository.existsByAlunoIdAndAnoLetivoAndSituacao(
                    origem.getAlunoId(), turmaDestino.anoLetivo(), SituacaoMatricula.ATIVA)) {
                ignorados.add(origem.getAlunoNome() + " (" + origem.getAlunoRa() + ") - já matriculado");
                continue;
            }
            // Verifica se o aluno ainda está ativo no aluno-service
            try {
                AlunoInfo aluno = buscarAluno(origem.getAlunoId());
                if (!aluno.ativo()) {
                    ignorados.add(origem.getAlunoNome() + " (inativo)");
                    continue;
                }
            } catch (ResponseStatusException e) {
                ignorados.add(origem.getAlunoNome() + " (não encontrado no aluno-service)");
                continue;
            }

            Matricula nova = new Matricula();
            nova.setNumero(gerarNumero(turmaDestino.anoLetivo()));
            nova.setAlunoId(origem.getAlunoId());
            nova.setAlunoNome(origem.getAlunoNome());
            nova.setAlunoRa(origem.getAlunoRa());
            nova.setTurmaId(turmaDestino.id());
            nova.setTurmaIdentificacao(turmaDestino.identificacao());
            nova.setAnoLetivo(turmaDestino.anoLetivo());
            nova.setSerie(turmaDestino.serie());
            nova.setTurno(Turno.valueOf(turmaDestino.turno()));
            nova.setDataMatricula(LocalDate.now());
            nova.setSituacao(SituacaoMatricula.ATIVA);
            matriculaRepository.save(nova);
            matriculados++;
        }

        return new RematriculaResult(total, matriculados, ignorados.size(), ignorados);
    }

    // ─── Chamadas via API Gateway ─────────────────────────────────────────────

    private AlunoInfo buscarAluno(Integer alunoId) {
        try {
            AlunoInfo aluno = restTemplate.getForObject(
                    gatewayUrl + "/api/alunos/" + alunoId, AlunoInfo.class);
            if (aluno == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado");
            }
            return aluno;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado: " + alunoId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao consultar aluno-service para alunoId={}", alunoId, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Aluno Service indisponível");
        }
    }

    private TurmaInfo buscarTurma(Integer turmaId) {
        try {
            TurmaInfo turma = restTemplate.getForObject(
                    gatewayUrl + "/api/turmas/" + turmaId, TurmaInfo.class);
            if (turma == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada");
            }
            return turma;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada: " + turmaId);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao consultar academico-service para turmaId={}", turmaId, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Acadêmico Service indisponível");
        }
    }

    // ─── Utilitários ──────────────────────────────────────────────────────────

    private String gerarNumero(int anoLetivo) {
        String ano = String.valueOf(anoLetivo);
        Integer max = matriculaRepository.findMaxNumeroByAno(ano);
        return "MAT" + ano + String.format("%05d", (max == null ? 0 : max) + 1);
    }

    private Matricula findOrThrow(Integer id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matrícula não encontrada"));
    }
}
