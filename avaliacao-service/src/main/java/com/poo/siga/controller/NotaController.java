package com.poo.siga.controller;

import com.poo.siga.model.Nota;
import com.poo.siga.service.NotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notas")
@RequiredArgsConstructor
@Tag(name = "Notas e Avaliações")
@SecurityRequirement(name = "bearerAuth")
public class NotaController {

    private final NotaService service;

    @GetMapping("/turma/{turmaId}/disciplina/{disciplinaId}")
    public ResponseEntity<List<Nota>> listarPorTurmaEDisciplina(
            @PathVariable Integer turmaId,
            @PathVariable Integer disciplinaId,
            @RequestParam Integer periodo) {
        return ResponseEntity.ok(service.listarPorTurmaEDisciplinaEPeriodo(turmaId, disciplinaId, periodo));
    }

    @GetMapping("/turma/{turmaId}/aluno/{alunoId}")
    public ResponseEntity<List<Nota>> listarPorTurmaEAluno(
            @PathVariable Integer turmaId,
            @PathVariable Integer alunoId) {
        return ResponseEntity.ok(service.listarPorTurmaEAluno(turmaId, alunoId));
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<Nota>> listarPorAluno(@PathVariable Integer alunoId) {
        return ResponseEntity.ok(service.listarPorAluno(alunoId));
    }

    @GetMapping("/aluno/{alunoId}/exists")
    @Operation(summary = "Verifica se o aluno possui notas lançadas (para validação inter-serviço)")
    public ResponseEntity<Boolean> existeNotaParaAluno(@PathVariable Integer alunoId) {
        return ResponseEntity.ok(service.existeNotaParaAluno(alunoId));
    }

    @PostMapping
    public ResponseEntity<Nota> registrar(@RequestBody @Valid Nota req, Principal principal) {
        // Mock ou pega do Principal
        String usuario = principal != null ? principal.getName() : "sistema";
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(req, usuario));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Nota> atualizar(@PathVariable Integer id,
                                           @RequestBody Map<String, Double> body,
                                           Principal principal) {
        String usuario = principal != null ? principal.getName() : "sistema";
        return ResponseEntity.ok(service.atualizar(id, body.get("valor"), usuario));
    }

    @GetMapping("/boletim")
    @Operation(summary = "Calcula a média bimestral ou anual")
    public ResponseEntity<Double> calcularMedia(
            @RequestParam Integer turmaId,
            @RequestParam Integer disciplinaId,
            @RequestParam Integer alunoId,
            @RequestParam(required = false) Integer periodo) {
        if (periodo != null) {
            return ResponseEntity.ok(service.calcularMediaBimestral(turmaId, disciplinaId, alunoId, periodo));
        }
        return ResponseEntity.ok(service.calcularMediaAnual(turmaId, disciplinaId, alunoId));
    }
}
