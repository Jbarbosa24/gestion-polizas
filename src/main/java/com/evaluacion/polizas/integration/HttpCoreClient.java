package com.evaluacion.polizas.integration;

import com.evaluacion.polizas.dto.CoreEventoRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
public class HttpCoreClient implements CoreClient {
    private final RestClient cliente;
    private final String apiKey;

    public HttpCoreClient(@Value("${app.core.url}") String url,
                          @Value("${app.api-key}") String apiKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.cliente = RestClient.builder().baseUrl(url).requestFactory(factory).build();
        this.apiKey = apiKey;
    }

    @Override
    public void notificarActualizacion(Long polizaId) {
        try {
            cliente.post()
                    .uri("")
                    .header("x-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CoreEventoRequest("ACTUALIZACION", polizaId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new CoreNoDisponibleException(
                    "No fue posible registrar la actualización en el CORE", ex);
        }
    }
}
