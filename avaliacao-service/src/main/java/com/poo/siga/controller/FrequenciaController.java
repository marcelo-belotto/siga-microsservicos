package com.poo.siga.controller;

import com.poo.siga.model.Frequencia;
import com.poo.siga.service.FrequenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/frequencias")
@RequiredArgsConstructor
@Tag(name = "Frequências")
@SecurityRequirement(name = "bearerAuth")
public class FrequenciaController {

    private final FrequenciaService service;

    @GetMapping("/diario")
    @Operation(summary = "Lista as frequências de uma turma/disciplina em uma data específica")
    public ResponseEntity<List<Frequencia>> listarPorTurmaEData(
            @RequestParam Integer turmaId,
            @RequestParam Integer disciplinaId,
            @RequestParam String data) {
        return ResponseEntity.ok(service.listarPorTurmaEData(turmaId, disciplinaId, data));
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<Frequencia>> listarPorAluno(@PathVariable Integer alunoId) {
        return ResponseEntity.ok(service.listarPorAluno(alunoId));
    }

    @PostMapping
    public ResponseEntity<Frequencia> registrar(@RequestBody @Valid Frequencia req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Frequencia> atualizar(@PathVariable Integer id,
                                                 @RequestBody @Valid Frequencia req) {
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @GetMapping("/aluno/{alunoId}/disciplina/{disciplinaId}/percentual")
    @Operation(summary = "Calcula o % de presença efetiva do aluno na disciplina")
    public ResponseEntity<Double> percentualPresenca(
            @PathVariable Integer alunoId,
            @PathVariable Integer disciplinaId) {
        return ResponseEntity.ok(service.calcularPercentualPresenca(alunoId, disciplinaId));
    }
}
