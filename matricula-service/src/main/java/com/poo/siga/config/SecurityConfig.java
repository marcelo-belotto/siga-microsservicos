package com.poo.siga.config;

import com.poo.siga.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(new JwtForwardInterceptor()));
        return restTemplate;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/actuator/health").permitAll();
                    auth.requestMatchers("/actuator/**").hasRole("ADMIN");

                    // Endpoints de verificação inter-serviço — autenticado via JWT
                    auth.requestMatchers(HttpMethod.GET, "/api/matriculas/aluno/*/exists").authenticated();
                    auth.requestMatchers(HttpMethod.GET, "/api/matriculas/turma/*/exists").authenticated();

                    // MATRICULAS CRUD
                    auth.requestMatchers(HttpMethod.POST, "/api/matriculas/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/matriculas/**")
                            .hasAnyRole("ADMIN", "COORDENADOR", "OPERADOR");

                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> res.setStatus(401)));

        return http.build();
    }
}
