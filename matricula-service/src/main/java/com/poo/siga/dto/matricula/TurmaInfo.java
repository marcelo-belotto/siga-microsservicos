package com.poo.siga.dto.matricula;

/**
 * DTO com os dados mínimos de Turma obtidos via chamada HTTP ao Acadêmico Service
 * através do API Gateway. Evita acoplamento direto com o banco de outro domínio.
 */
public record TurmaInfo(
    Integer id,
    Integer anoLetivo,
    Integer serie,
    String turno,
    String identificacao,
    boolean ativo
) {}
