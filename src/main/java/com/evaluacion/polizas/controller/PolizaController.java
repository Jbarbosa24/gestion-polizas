package com.evaluacion.polizas.controller;

import com.evaluacion.polizas.dto.*;
import com.evaluacion.polizas.model.*;
import com.evaluacion.polizas.service.PolizaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/polizas")
public class PolizaController {
    private final PolizaService servicio;

    public PolizaController(PolizaService servicio) { this.servicio = servicio; }

    @GetMapping
    public List<PolizaResponse> listar(
            @RequestParam(required = false) TipoPoliza tipo,
            @RequestParam(required = false) EstadoPoliza estado) {
        return servicio.listar(tipo, estado);
    }

    @GetMapping("/{id}/riesgos")
    public List<RiesgoResponse> listarRiesgos(@PathVariable Long id) {
        return servicio.listarRiesgos(id);
    }

    @PostMapping("/{id}/renovar")
    public PolizaResponse renovar(@PathVariable Long id, @Valid @RequestBody RenovarRequest request) {
        return servicio.renovar(id, request.ipc());
    }

    @PostMapping("/{id}/cancelar")
    public PolizaResponse cancelar(@PathVariable Long id) {
        return servicio.cancelar(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/riesgos")
    public RiesgoResponse agregarRiesgo(@PathVariable Long id,
                                        @Valid @RequestBody CrearRiesgoRequest request) {
        return servicio.agregarRiesgo(id, request);
    }
}
