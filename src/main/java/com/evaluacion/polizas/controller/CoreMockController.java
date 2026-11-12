package com.evaluacion.polizas.controller;

import com.evaluacion.polizas.dto.CoreEventoRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/core-mock")
public class CoreMockController {
    private static final Logger log = LoggerFactory.getLogger(CoreMockController.class);

    @PostMapping("/evento")
    public ResponseEntity<Map<String, String>> recibir(@Valid @RequestBody CoreEventoRequest evento) {
        log.info("Intento de actualización en CORE: evento={}, polizaId={}",
                evento.evento(), evento.polizaId());
        return ResponseEntity.accepted().body(Map.of("estado", "REGISTRADO"));
    }
}
