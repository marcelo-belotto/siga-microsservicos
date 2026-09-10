package com.poo.siga.model;

import com.poo.siga.model.enums.SituacaoMatricula;
import com.poo.siga.model.enums.Turno;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "matricula")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String numero;

    // Desacoplado: apenas o ID do aluno (dado de outro domínio)
    @NotNull
    @Column(name = "aluno_id", nullable = false)
    private Integer alunoId;

    // Campos desnormalizados do aluno para resposta rápida sem join remoto
    @Column(name = "aluno_nome")
    private String alunoNome;

    @Column(name = "aluno_ra")
    private String alunoRa;

    // Desacoplado: apenas o ID da turma (dado de outro domínio)
    @NotNull
    @Column(name = "turma_id", nullable = false)
    private Integer turmaId;

    // Campos desnormalizados da turma para resposta rápida sem join remoto
    @Column(name = "turma_identificacao")
    private String turmaIdentificacao;

    @NotNull
    @Column(name = "ano_letivo", nullable = false)
    private Integer anoLetivo;

    @NotNull
    @Column(nullable = false)
    private Integer serie;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Turno turno;

    @Column(nullable = false)
    private LocalDate dataMatricula;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoMatricula situacao = SituacaoMatricula.ATIVA;

    private String motivoCancelamento;

    private LocalDate dataCancelamento;

    @PrePersist
    public void prePersist() {
        if (this.dataMatricula == null) {
            this.dataMatricula = LocalDate.now();
        }
    }
}