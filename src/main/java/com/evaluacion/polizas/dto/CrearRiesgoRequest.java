package com.evaluacion.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearRiesgoRequest(
        @NotBlank @Size(max = 120) String arrendatario,
        @NotBlank @Size(max = 160) String inmueble) { }
