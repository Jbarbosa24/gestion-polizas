package com.evaluacion.polizas;

import com.evaluacion.polizas.integration.CoreClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:polizas_http;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class ApiHttpTests {
    @Autowired MockMvc mvc;
    @MockitoBean CoreClient core;

    @Test
    void sinApiKeyRetorna401() throws Exception {
        mvc.perform(get("/polizas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conApiKeyPermiteListar() throws Exception {
        mvc.perform(get("/polizas").header("x-api-key", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void filtraPorTipoYEstado() throws Exception {
        mvc.perform(get("/polizas")
                        .header("x-api-key", "123456")
                        .queryParam("tipo", "COLECTIVA")
                        .queryParam("estado", "ACTIVA"))
                .andExpect(status().isOk());
    }

    @Test
    void rechazoAlAgregarRiesgoEnIndividual() throws Exception {
        mvc.perform(post("/polizas/1/riesgos")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"arrendatario\":\"Ana\",\"inmueble\":\"Casa 8\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void mockRegistraEvento() throws Exception {
        mvc.perform(post("/core-mock/evento")
                        .header("x-api-key", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.estado").value("REGISTRADO"));
    }
}
