package com.poo.siga.config;

import com.poo.siga.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;

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
        return new RestTemplate();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // Actuator — apenas health básico é público; demais exigem ADMIN
                    auth.requestMatchers("/actuator/health").permitAll();
                    auth.requestMatchers("/actuator/**").hasRole("ADMIN");

                    // === MODELOS DE GRADE: ADMIN ou COORDENADOR ===
                    auth.requestMatchers("/api/modelos-grade/**").hasAnyRole("ADMIN", "COORDENADOR");

                    // === TURMAS: ADMIN ou COORDENADOR ===
                    auth.requestMatchers(HttpMethod.POST, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR");
                    auth.requestMatchers(HttpMethod.PUT, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/turmas/**").hasAnyRole("ADMIN", "COORDENADOR");

                    // === PROFESSORES: Somente ADMIN (exceto atualização, que também permite COORDENADOR) ===
                    auth.requestMatchers(HttpMethod.POST, "/api/professores/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/professores/**").hasAnyRole("ADMIN", "COORDENADOR");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/professores/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/professores/**").hasRole("ADMIN");

                    // === DISCIPLINAS: Somente ADMIN ===
                    auth.requestMatchers(HttpMethod.POST, "/api/disciplinas/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/disciplinas/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/disciplinas/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/disciplinas/**").hasRole("ADMIN");

                    // === ANO LETIVO: Somente ADMIN pode criar/alterar/encerrar ===
                    auth.requestMatchers(HttpMethod.POST, "/api/anos-letivos/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/anos-letivos/**").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/anos-letivos/**").hasRole("ADMIN");

                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                        }));

        return http.build();
    }
}
