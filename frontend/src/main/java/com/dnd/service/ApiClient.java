package com.dnd.service;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ApiClient {

    private final WebClient webClient;

    public ApiClient(@Value("${api.backend.url}") String backendUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(backendUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private Optional<String> getToken() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            return Optional.ofNullable(session.getAttribute("jwt_token"))
                    .map(Object::toString);
        }
        return Optional.empty();
    }

    // GET request
    public <T> T get(String path, Class<T> responseType) {
        WebClient.RequestHeadersSpec<?> spec = webClient.get().uri(path);
        Optional<String> token = getToken();
        if (token.isPresent()) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token.get());
        }
        return spec.retrieve()
                .bodyToMono(responseType)
                .block();
    }

    // GET list
    public <T> List<T> getList(String path, Class<T> elementType) {
        WebClient.RequestHeadersSpec<?> spec = webClient.get().uri(path);
        Optional<String> token = getToken();
        if (token.isPresent()) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token.get());
        }
        return spec.retrieve()
                .bodyToFlux(elementType)
                .collectList()
                .block();
    }

    // POST request
    public <T, R> R post(String path, T body, Class<R> responseType) {
        WebClient.RequestBodySpec bodySpec = webClient.post().uri(path);
        Optional<String> token = getToken();
        if (token.isPresent()) {
            bodySpec = bodySpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token.get());
        }
        return bodySpec.bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    // POST without auth (for login/register)
    public <T, R> R postPublic(String path, T body, Class<R> responseType) {
        return webClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    // PUT request
    public <T, R> R put(String path, T body, Class<R> responseType) {
        WebClient.RequestBodySpec bodySpec = webClient.put().uri(path);
        Optional<String> token = getToken();
        if (token.isPresent()) {
            bodySpec = bodySpec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token.get());
        }
        return bodySpec.bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    // DELETE request
    public void delete(String path) {
        WebClient.RequestHeadersSpec<?> spec = webClient.delete().uri(path);
        Optional<String> token = getToken();
        if (token.isPresent()) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token.get());
        }
        spec.retrieve()
                .toBodilessEntity()
                .block();
    }

    // Check if request will fail with 401
    public boolean isAuthenticated() {
        try {
            get("/api/auth/me", Map.class);
            return true;
        } catch (WebClientResponseException.Unauthorized e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
