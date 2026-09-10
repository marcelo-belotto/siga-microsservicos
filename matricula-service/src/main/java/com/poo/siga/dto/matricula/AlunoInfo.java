package com.poo.siga.dto.matricula;

/**
 * DTO com os dados mínimos de Aluno obtidos via chamada HTTP ao Aluno Service
 * através do API Gateway.
 */
public record AlunoInfo(
    Integer id,
    String nome,
    String ra,
    boolean ativo
) {}
