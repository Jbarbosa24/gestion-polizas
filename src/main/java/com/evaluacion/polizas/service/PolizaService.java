package com.evaluacion.polizas.service;

import com.evaluacion.polizas.dto.*;
import com.evaluacion.polizas.integration.CoreClient;
import com.evaluacion.polizas.model.*;
import com.evaluacion.polizas.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PolizaService {
    private final PolizaRepository polizas;
    private final RiesgoRepository riesgos;
    private final CoreClient core;

    public PolizaService(PolizaRepository polizas, RiesgoRepository riesgos, CoreClient core) {
        this.polizas = polizas;
        this.riesgos = riesgos;
        this.core = core;
    }

    @Transactional(readOnly = true)
    public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
        List<Poliza> resultado;
        if (tipo != null && estado != null) {
            resultado = polizas.findByTipoAndEstado(tipo, estado);
        } else if (tipo != null) {
            resultado = polizas.findByTipo(tipo);
        } else if (estado != null) {
            resultado = polizas.findByEstado(estado);
        } else {
            resultado = polizas.findAll();
        }
        return resultado.stream().map(PolizaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<RiesgoResponse> listarRiesgos(Long polizaId) {
        buscarPoliza(polizaId);
        return riesgos.findByPolizaIdOrderByIdAsc(polizaId).stream()
                .map(RiesgoResponse::desde).toList();
    }

    @Transactional
    public RiesgoResponse agregarRiesgo(Long polizaId, CrearRiesgoRequest request) {
        Poliza poliza = buscarPoliza(polizaId);
        if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
            throw new IllegalStateException("Solo se pueden agregar riesgos a pólizas colectivas");
        }
        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new IllegalStateException("No se pueden modificar pólizas canceladas");
        }
        core.notificarActualizacion(polizaId);
        Riesgo riesgo = riesgos.save(new Riesgo(poliza, request.arrendatario(), request.inmueble()));
        return RiesgoResponse.desde(riesgo);
    }

    @Transactional
    public PolizaResponse renovar(Long id, BigDecimal ipc) {
        Poliza poliza = buscarPoliza(id);
        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new IllegalStateException("No se puede renovar una póliza cancelada");
        }
        if (ipc == null || ipc.signum() < 0 || ipc.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("El IPC debe estar entre 0 y 1");
        }

        BigDecimal nuevoCanon = poliza.getCanonMensual()
                .multiply(BigDecimal.ONE.add(ipc))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal nuevaPrima = nuevoCanon
                .multiply(BigDecimal.valueOf(poliza.getMesesVigencia()))
                .setScale(2, RoundingMode.HALF_UP);
        LocalDate nuevoInicio = poliza.getFechaFin().plusDays(1);
        LocalDate nuevoFin = nuevoInicio.plusMonths(poliza.getMesesVigencia()).minusDays(1);

        core.notificarActualizacion(id);
        poliza.setCanonMensual(nuevoCanon);
        poliza.setPrima(nuevaPrima);
        poliza.setFechaInicio(nuevoInicio);
        poliza.setFechaFin(nuevoFin);
        poliza.setEstado(EstadoPoliza.RENOVADA);
        return PolizaResponse.desde(polizas.save(poliza));
    }

    @Transactional
    public PolizaResponse cancelar(Long id) {
        Poliza poliza = buscarPoliza(id);
        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            return PolizaResponse.desde(poliza);
        }
        List<Riesgo> riesgosAsociados = riesgos.findByPolizaIdOrderByIdAsc(id);
        core.notificarActualizacion(id);
        poliza.setEstado(EstadoPoliza.CANCELADA);
        for (Riesgo r : riesgosAsociados) {
            r.setEstado(EstadoRiesgo.CANCELADO);
        }
        riesgos.saveAll(riesgosAsociados);
        return PolizaResponse.desde(polizas.save(poliza));
    }

    @Transactional
    public RiesgoResponse cancelarRiesgo(Long id) {
        Riesgo riesgo = riesgos.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Riesgo no encontrado: " + id));
        if (riesgo.getEstado() == EstadoRiesgo.CANCELADO) {
            return RiesgoResponse.desde(riesgo);
        }
        core.notificarActualizacion(riesgo.getPoliza().getId());
        riesgo.setEstado(EstadoRiesgo.CANCELADO);
        return RiesgoResponse.desde(riesgos.save(riesgo));
    }

    private Poliza buscarPoliza(Long id) {
        return polizas.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Póliza no encontrada: " + id));
    }
}
