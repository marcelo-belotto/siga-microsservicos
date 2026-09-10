package com.poo.siga.repository;

import com.poo.siga.model.GradeHoraria;
import com.poo.siga.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GradeHorariaRepository extends JpaRepository<GradeHoraria, Integer> {
    List<GradeHoraria> findByTurmaId(Integer turmaId);
    List<GradeHoraria> findByTurmaIdAndDiaSemana(Integer turmaId, DayOfWeek diaSemana);
    Optional<GradeHoraria> findByTurmaIdAndDiaSemanaAndNumeroAula(Integer turmaId, DayOfWeek diaSemana, Integer numeroAula);
    void deleteByTurma(Turma turma);

    // Nota de migração: no monolito esta consulta fazia JOIN com a entidade "Usuario"
    // (tabela de autenticação) para resolver o professor a partir do login do JWT.
    // Como "Usuario" pertence ao domínio de autenticação (fora do academico-service),
    // o vínculo agora é feito pelo codigoFuncional do Professor, que é o identificador
    // único deste domínio e é o valor esperado no parâmetro "codigoFuncional" (extraído
    // do subject do JWT/claims pelo chamador, via API Gateway).
    @Query("SELECT DISTINCT tdp.disciplina.id FROM TurmaDisciplinaProfessor tdp " +
           "WHERE tdp.turma.id = :turmaId " +
           "AND tdp.professor.codigoFuncional = :codigoFuncional")
    Set<Integer> findDisciplinaIdsByTurmaAndProfessorCodigoFuncional(
        @Param("turmaId") Integer turmaId,
        @Param("codigoFuncional") String codigoFuncional
    );
}
