package com.poo.siga.model;

import com.poo.siga.model.enums.StatusFrequencia;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "frequencia", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"turma_id", "disciplina_id", "aluno_id", "data", "numero_aula"})
})
public class Frequencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "turma_id", nullable = false)
    private Integer turmaId;

    @NotNull
    @Column(name = "disciplina_id", nullable = false)
    private Integer disciplinaId;

    @NotNull
    @Column(name = "aluno_id", nullable = false)
    private Integer alunoId;

    @NotNull(message = "A data é obrigatória")
    @Column(nullable = false)
    private LocalDate data;

    @NotNull(message = "O número da aula é obrigatório")
    @Min(value = 1, message = "O número da aula mínimo é 1")
    @Max(value = 5, message = "O número da aula máximo é 5")
    @Column(name = "numero_aula", nullable = false)
    private Integer numeroAula;

    @NotNull(message = "O status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFrequencia status;

    private String justificativa;
}
