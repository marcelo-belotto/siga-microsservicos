package com.poo.siga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

/**
 * Configuração central de roteamento do API Gateway.
 * Cada rota encaminha um prefixo de path para o microsserviço correspondente,
 * conforme o domínio migrado do monolito.
 */
@Configuration
public class RouteConfig {

    @Value("${services.academico.url}")
    private String academicoUrl;

    @Value("${services.aluno.url}")
    private String alunoUrl;

    @Value("${services.matricula.url}")
    private String matriculaUrl;

    @Value("${services.avaliacao.url}")
    private String avaliacaoUrl;

    // === ACADEMICO SERVICE: turmas, professores, disciplinas, modelos de grade, anos letivos ===

    @Bean
    public RouterFunction<ServerResponse> turmasRoute() {
        return route("turmas_route")
                .route(path("/api/turmas/**"), http())
                .before(uri(academicoUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> professoresRoute() {
        return route("professores_route")
                .route(path("/api/professores/**"), http())
                .before(uri(academicoUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> disciplinasRoute() {
        return route("disciplinas_route")
                .route(path("/api/disciplinas/**"), http())
                .before(uri(academicoUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> modelosGradeRoute() {
        return route("modelos_grade_route")
                .route(path("/api/modelos-grade/**"), http())
                .before(uri(academicoUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> anosLetivosRoute() {
        return route("anos_letivos_route")
                .route(path("/api/anos-letivos/**"), http())
                .before(uri(academicoUrl))
                .build();
    }

    // === ALUNO SERVICE ===

    @Bean
    public RouterFunction<ServerResponse> alunosRoute() {
        return route("alunos_route")
                .route(path("/api/alunos/**"), http())
                .before(uri(alunoUrl))
                .build();
    }

    // === MATRICULA SERVICE ===

    @Bean
    public RouterFunction<ServerResponse> matriculasRoute() {
        return route("matriculas_route")
                .route(path("/api/matriculas/**"), http())
                .before(uri(matriculaUrl))
                .build();
    }

    // === AVALIACAO SERVICE: notas e frequencias ===

    @Bean
    public RouterFunction<ServerResponse> notasRoute() {
        return route("notas_route")
                .route(path("/api/notas/**"), http())
                .before(uri(avaliacaoUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> frequenciasRoute() {
        return route("frequencias_route")
                .route(path("/api/frequencias/**"), http())
                .before(uri(avaliacaoUrl))
                .build();
    }
}
