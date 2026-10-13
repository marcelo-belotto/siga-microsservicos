package com.poo.siga.service;

import com.poo.siga.dto.aluno.*;
import com.poo.siga.model.Aluno;
import com.poo.siga.repository.AlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository repository;
    private final RestTemplate restTemplate;


    @Value("${gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    @Transactional(readOnly = true)
    public Page<AlunoResponse> listar(String nome, String ra, String cpf, Boolean ativo, Pageable pageable) {
        return repository.buscarComFiltros(nome, ra, cpf, ativo, pageable).map(AlunoResponse::from);
    }

    @Transactional(readOnly = true)
    public AlunoResponse buscarPorId(Integer id) {
        return AlunoResponse.from(findOrThrow(id));
    }

    @Transactional
    public AlunoResponse criar(AlunoRequest req) {
        if (repository.existsByCpf(req.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF do aluno já cadastrado");
        }
        var aluno = new Aluno();
        aluno.setRa(gerarProximoRa());
        aluno.setNome(req.nome());
        aluno.setDataNascimento(req.dataNascimento());
        aluno.setCpf(req.cpf());
        aluno.setEmail(req.email());
        aluno.setNomeResponsavel(req.nomeResponsavel());
        aluno.setCpfResponsavel(req.cpfResponsavel());
        return AlunoResponse.from(repository.save(aluno));
    }

    private String gerarProximoRa() {
        Integer maxNum = repository.findMaxRaNumber();
        int proximo = (maxNum == null ? 0 : maxNum) + 1;
        return "RA" + String.format("%06d", proximo);
    }

    @Transactional
    public AlunoResponse atualizar(Integer id, AlunoRequest req) {
        var aluno = findOrThrow(id);
        aluno.setNome(req.nome());
        aluno.setDataNascimento(req.dataNascimento());
        aluno.setCpf(req.cpf());
        aluno.setEmail(req.email());
        aluno.setNomeResponsavel(req.nomeResponsavel());
        aluno.setCpfResponsavel(req.cpfResponsavel());
        return AlunoResponse.from(repository.save(aluno));
    }

    @Transactional
    public void inativar(Integer id) {
        var aluno = findOrThrow(id);
        aluno.setAtivo(false);
        repository.save(aluno);
    }

    @Transactional
    public void ativar(Integer id) {
        var aluno = findOrThrow(id);
        aluno.setAtivo(true);
        repository.save(aluno);
    }

    @Transactional
    public void excluir(Integer id) {
        var aluno = findOrThrow(id);
        
        // Chamada síncrona via RestTemplate passando pelo API Gateway
        try {
            Boolean hasMatricula = restTemplate.getForObject(gatewayUrl + "/api/matriculas/aluno/" + id + "/exists", Boolean.class);
            if (Boolean.TRUE.equals(hasMatricula)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível excluir aluno com matrículas. Utilize a inativação.");
            }
            
            Boolean hasNota = restTemplate.getForObject(gatewayUrl + "/api/notas/aluno/" + id + "/exists", Boolean.class);
            if (Boolean.TRUE.equals(hasNota)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível excluir aluno com notas lançadas. Utilize a inativação.");
            }
        } catch (ResponseStatusException ex) {
            // Re-lança erros HTTP intencionais (ex.: 409 CONFLICT)
            throw ex;
        } catch (Exception ex) {
            // Serviço dependente indisponível — bloqueamos a exclusão por segurança (fail-safe)
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Serviços dependentes indisponíveis para validação de exclusão.");
        }
        
        repository.delete(aluno);
    }

    private Aluno findOrThrow(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
    }
}
