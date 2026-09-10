package com.poo.siga.dto.matricula;

import com.poo.siga.model.Matricula;
import java.time.LocalDate;

public record MatriculaResponse(
    Integer id,
    String numero,
    Integer alunoId,
    String alunoNome,
    String alunoRa,
    Integer turmaId,
    String turmaIdentificacao,
    Integer anoLetivo,
    Integer serie,
    String turno,
    LocalDate dataMatricula,
    String situacao,
    String motivoCancelamento,
    LocalDate dataCancelamento
) {
    public static MatriculaResponse from(Matricula m) {
        return new MatriculaResponse(
            m.getId(),
            m.getNumero(),
            m.getAlunoId(),
            m.getAlunoNome(),
            m.getAlunoRa(),
            m.getTurmaId(),
            m.getTurmaIdentificacao(),
            m.getAnoLetivo(),
            m.getSerie(),
            m.getTurno() != null ? m.getTurno().name() : null,
            m.getDataMatricula(),
            m.getSituacao() != null ? m.getSituacao().name() : null,
            m.getMotivoCancelamento(),
            m.getDataCancelamento()
        );
    }
}
