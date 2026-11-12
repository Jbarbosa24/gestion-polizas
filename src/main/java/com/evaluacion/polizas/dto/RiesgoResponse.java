package com.evaluacion.polizas.dto;

import com.evaluacion.polizas.model.*;

public record RiesgoResponse(
        Long id, Long polizaId, String arrendatario, String inmueble, EstadoRiesgo estado) {

    public static RiesgoResponse desde(Riesgo r) {
        return new RiesgoResponse(r.getId(), r.getPoliza().getId(),
                r.getArrendatario(), r.getInmueble(), r.getEstado());
    }
}
