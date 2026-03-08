package com.recipebook.service;

import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Service
public class ApiClient {

    private static final Logger LOG = LoggerFactory.getLogger(ApiClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String backendUrl;

    public ApiClient(@Value("${api.backend.url}") String backendUrl) {
        this.backendUrl = backendUrl;
        this.webClient = WebClient.builder()
                .baseUrl(backendUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public WebClient getWebClient() {
        return webClient;
    }

    private Optional<String> getToken() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            return Optional.ofNullable(session.getAttribute("jwt_token"))
                    .map(Object::toString);
        }
        return Optional.empty();
    }

    private <T> T execute(String query, Map<String, Object> variables, Class<T> responseType,
                          String dataField, boolean withAuth) {
        Map<String, Object> body = Map.of("query", query, "variables", variables != null ? variables : Map.of());

        WebClient.RequestBodySpec spec = webClient.post().uri("/graphql");
        if (withAuth) {
            getToken().ifPresent(token ->
                    spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        }

        String json;
        try {
            json = spec.bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientRequestException e) {
            LOG.error("Backend unavailable: {}", e.getMessage());
            throw new BackendUnavailableException("Server is unavailable. Please try again later.", e);
        }

        try {
            JsonNode root = objectMapper.readTree(json);

            if (root.has("errors") && root.get("errors").isArray() && !root.get("errors").isEmpty()) {
                String message = root.get("errors").get(0).path("message").asText("GraphQL error");
                throw new RuntimeException(message);
            }

            JsonNode dataNode = root.path("data").path(dataField);
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                return null;
            }
            return objectMapper.treeToValue(dataNode, responseType);
        } catch (JacksonException e) {
            LOG.error("Failed to parse GraphQL response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse GraphQL response: " + e.getMessage(), e);
        }
    }

    /**
     * Thrown when the backend API cannot be reached.
     */
    public static class BackendUnavailableException extends RuntimeException {
        public BackendUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public <T> T query(String query, Map<String, Object> variables, Class<T> responseType, String dataField) {
        return execute(query, variables, responseType, dataField, true);
    }

    public <T> T queryPublic(String query, Map<String, Object> variables, Class<T> responseType, String dataField) {
        return execute(query, variables, responseType, dataField, false);
    }

    public <T> T mutate(String query, Map<String, Object> variables, Class<T> responseType, String dataField) {
        return execute(query, variables, responseType, dataField, true);
    }

    public <T> T mutatePublic(String query, Map<String, Object> variables, Class<T> responseType, String dataField) {
        return execute(query, variables, responseType, dataField, false);
    }
}
