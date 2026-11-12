package com.evaluacion.polizas.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

// Ejemplo: 0.05 equivale al 5 % de incremento del IPC.
public record RenovarRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal ipc) { }
