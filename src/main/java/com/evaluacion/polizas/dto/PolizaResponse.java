package com.evaluacion.polizas.dto;

import com.evaluacion.polizas.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PolizaResponse(
        Long id, TipoPoliza tipo, EstadoPoliza estado,
        BigDecimal canonMensual, BigDecimal prima,
        Integer mesesVigencia, LocalDate fechaInicio, LocalDate fechaFin) {

    public static PolizaResponse desde(Poliza p) {
        return new PolizaResponse(p.getId(), p.getTipo(), p.getEstado(),
                p.getCanonMensual(), p.getPrima(), p.getMesesVigencia(),
                p.getFechaInicio(), p.getFechaFin());
    }
}
