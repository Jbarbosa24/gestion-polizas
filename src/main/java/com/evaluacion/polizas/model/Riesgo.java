package com.evaluacion.polizas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "riesgos")
public class Riesgo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poliza_id", nullable = false)
    private Poliza poliza;

    @Column(nullable = false, length = 120)
    private String arrendatario;

    @Column(nullable = false, length = 160)
    private String inmueble;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRiesgo estado = EstadoRiesgo.ACTIVO;

    protected Riesgo() { }

    public Riesgo(Poliza poliza, String arrendatario, String inmueble) {
        this.poliza = poliza;
        this.arrendatario = arrendatario;
        this.inmueble = inmueble;
    }

    public Long getId() { return id; }
    public Poliza getPoliza() { return poliza; }
    public String getArrendatario() { return arrendatario; }
    public String getInmueble() { return inmueble; }
    public EstadoRiesgo getEstado() { return estado; }
    public void setEstado(EstadoRiesgo estado) { this.estado = estado; }
}
