package com.evaluacion.polizas.config;

import com.evaluacion.polizas.model.*;
import com.evaluacion.polizas.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DatosIniciales {
    @Bean
    CommandLineRunner datosDemostracion(PolizaRepository polizas, RiesgoRepository riesgos) {
        return args -> {
            if (polizas.count() != 0) return;
            Poliza individual = polizas.save(new Poliza(
                    TipoPoliza.INDIVIDUAL, new BigDecimal("1000000.00"), 12, LocalDate.of(2026, 1, 1)));
            Poliza colectiva = polizas.save(new Poliza(
                    TipoPoliza.COLECTIVA, new BigDecimal("1800000.00"), 12, LocalDate.of(2026, 2, 1)));
            riesgos.save(new Riesgo(individual, "Arrendatario individual", "Apartamento 101"));
            riesgos.save(new Riesgo(colectiva, "Arrendatario A", "Apartamento 201"));
            riesgos.save(new Riesgo(colectiva, "Arrendatario B", "Apartamento 202"));
        };
    }
}
