package com.evaluacion.polizas.controller;

import com.evaluacion.polizas.dto.RiesgoResponse;
import com.evaluacion.polizas.service.PolizaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/riesgos")
public class RiesgoController {
    private final PolizaService servicio;

    public RiesgoController(PolizaService servicio) { this.servicio = servicio; }

    @PostMapping("/{id}/cancelar")
    public RiesgoResponse cancelar(@PathVariable Long id) {
        return servicio.cancelarRiesgo(id);
    }
}
