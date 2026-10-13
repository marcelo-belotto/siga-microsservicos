package com.poo.siga.config;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

/**
 * Interceptor que propaga o token JWT da requisição de entrada para as
 * chamadas inter-serviço feitas pelo RestTemplate.
 * Isso é necessário para que o api-gateway aceite as chamadas internas,
 * pois exige autenticação em todas as rotas (exceto /auth/login).
 */
public class JwtForwardInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            String authorization = attributes.getRequest().getHeader("Authorization");
            if (authorization != null && !authorization.isBlank()) {
                request.getHeaders().set("Authorization", authorization);
            }
        }

        return execution.execute(request, body);
    }
}
