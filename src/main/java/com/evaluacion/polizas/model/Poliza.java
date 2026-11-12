package com.evaluacion.polizas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "polizas")
public class Poliza {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPoliza estado;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal canonMensual;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal prima;

    @Column(nullable = false)
    private Integer mesesVigencia;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Version
    private Long version;

    protected Poliza() { }

    public Poliza(TipoPoliza tipo, BigDecimal canonMensual, int mesesVigencia, LocalDate fechaInicio) {
        if (mesesVigencia < 1 || canonMensual == null || canonMensual.signum() <= 0) {
            throw new IllegalArgumentException("El canon y los meses de vigencia deben ser positivos");
        }
        this.tipo = tipo;
        this.estado = EstadoPoliza.ACTIVA;
        this.canonMensual = canonMensual;
        this.mesesVigencia = mesesVigencia;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaInicio.plusMonths(mesesVigencia).minusDays(1);
        this.prima = canonMensual.multiply(BigDecimal.valueOf(mesesVigencia));
    }

    public Long getId() { return id; }
    public TipoPoliza getTipo() { return tipo; }
    public EstadoPoliza getEstado() { return estado; }
    public void setEstado(EstadoPoliza estado) { this.estado = estado; }
    public BigDecimal getCanonMensual() { return canonMensual; }
    public void setCanonMensual(BigDecimal canonMensual) { this.canonMensual = canonMensual; }
    public BigDecimal getPrima() { return prima; }
    public void setPrima(BigDecimal prima) { this.prima = prima; }
    public Integer getMesesVigencia() { return mesesVigencia; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
}
