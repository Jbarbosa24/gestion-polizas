package com.evaluacion.polizas;

import com.evaluacion.polizas.dto.*;
import com.evaluacion.polizas.integration.CoreClient;
import com.evaluacion.polizas.model.*;
import com.evaluacion.polizas.repository.*;
import com.evaluacion.polizas.service.PolizaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:polizas_service;DB_CLOSE_DELAY=-1")
class PolizaServiceTests {
    @Autowired PolizaService servicio;
    @Autowired PolizaRepository polizas;
    @Autowired RiesgoRepository riesgos;
    @MockitoBean CoreClient core;

    @BeforeEach
    void preparar() {
        // La prueba usa la base H2 y los datos de demostración; repone estados entre casos.
        riesgos.deleteAll();
        polizas.deleteAll();
    }

    private Poliza nuevaPoliza(TipoPoliza tipo) {
        return polizas.save(new Poliza(tipo, new BigDecimal("1000000.00"),
                12, java.time.LocalDate.of(2026, 1, 1)));
    }

    @Test
    void renovarAplicaIpcYActualizaVigencia() {
        Poliza p = nuevaPoliza(TipoPoliza.INDIVIDUAL);
        PolizaResponse renovada = servicio.renovar(p.getId(), new BigDecimal("0.05"));
        assertEquals(0, new BigDecimal("1050000.00").compareTo(renovada.canonMensual()));
        assertEquals(0, new BigDecimal("12600000.00").compareTo(renovada.prima()));
        assertEquals(EstadoPoliza.RENOVADA, renovada.estado());
        assertEquals(java.time.LocalDate.of(2027, 1, 1), renovada.fechaInicio());
        verify(core).notificarActualizacion(p.getId());
    }

    @Test
    void noRenuevaPolizaCancelada() {
        Poliza p = nuevaPoliza(TipoPoliza.INDIVIDUAL);
        servicio.cancelar(p.getId());
        assertThrows(IllegalStateException.class,
                () -> servicio.renovar(p.getId(), new BigDecimal("0.05")));
    }

    @Test
    void noAgregaRiesgoAIndividual() {
        Poliza p = nuevaPoliza(TipoPoliza.INDIVIDUAL);
        assertThrows(IllegalStateException.class,
                () -> servicio.agregarRiesgo(p.getId(), new CrearRiesgoRequest("Ana", "Casa 8")));
        verifyNoInteractions(core);
    }

    @Test
    void permiteAgregarRiesgoAColectiva() {
        Poliza p = nuevaPoliza(TipoPoliza.COLECTIVA);
        RiesgoResponse r = servicio.agregarRiesgo(p.getId(), new CrearRiesgoRequest("Ana", "Casa 8"));
        assertEquals(p.getId(), r.polizaId());
        assertEquals(EstadoRiesgo.ACTIVO, r.estado());
        verify(core).notificarActualizacion(p.getId());
    }

    @Test
    void cancelarPolizaCancelaTodosSusRiesgos() {
        Poliza p = nuevaPoliza(TipoPoliza.COLECTIVA);
        riesgos.saveAll(List.of(new Riesgo(p, "Ana", "Casa A"), new Riesgo(p, "Beto", "Casa B")));
        servicio.cancelar(p.getId());
        assertEquals(EstadoPoliza.CANCELADA, polizas.findById(p.getId()).orElseThrow().getEstado());
        assertTrue(riesgos.findByPolizaIdOrderByIdAsc(p.getId()).stream()
                .allMatch(r -> r.getEstado() == EstadoRiesgo.CANCELADO));
    }

    @Test
    void siFallaCoreNoPersisteRenovacion() {
        Poliza p = nuevaPoliza(TipoPoliza.COLECTIVA);
        doThrow(new IllegalStateException("CORE temporalmente no disponible"))
                .when(core).notificarActualizacion(p.getId());
        assertThrows(IllegalStateException.class,
                () -> servicio.renovar(p.getId(), new BigDecimal("0.03")));
        assertEquals(EstadoPoliza.ACTIVA, polizas.findById(p.getId()).orElseThrow().getEstado());
    }
}
