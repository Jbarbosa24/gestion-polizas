package com.evaluacion.polizas.repository;

import com.evaluacion.polizas.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {
    List<Poliza> findByTipo(TipoPoliza tipo);
    List<Poliza> findByEstado(EstadoPoliza estado);
    List<Poliza> findByTipoAndEstado(TipoPoliza tipo, EstadoPoliza estado);
}
