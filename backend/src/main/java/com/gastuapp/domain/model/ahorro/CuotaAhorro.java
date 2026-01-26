package com.gastuapp.domain.model.ahorro;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo de Dominio: CuotaAhorro
 * 
 * <p>
 * Representa una cuota programada dentro de una Meta de Ahorro.
 * Permite planificar los pagos futuros y controlar el progreso detallado.
 * </p>
 * 
 * @author Juan Esteban Barrios Portela
 * @version 1.0
 * @since 2026-01-26
 */
public class CuotaAhorro {

    private Long id;
    private Long metaAhorroId;
    private Integer numeroCuota;
    private LocalDate fechaProgramada;
    private BigDecimal montoEsperado;
    private EstadoCuota estado;
    private Long ahorroId; // Referencia al abono real si ya se pagó

    public enum EstadoCuota {
        PENDIENTE,
        PAGADA,
        VENCIDA,
        CANCELADA
    }

    public CuotaAhorro() {
    }

    public CuotaAhorro(Long id, Long metaAhorroId, Integer numeroCuota, LocalDate fechaProgramada,
            BigDecimal montoEsperado, EstadoCuota estado) {
        this.id = id;
        this.metaAhorroId = metaAhorroId;
        this.numeroCuota = numeroCuota;
        this.fechaProgramada = fechaProgramada;
        this.montoEsperado = montoEsperado;
        this.estado = estado;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMetaAhorroId() {
        return metaAhorroId;
    }

    public void setMetaAhorroId(Long metaAhorroId) {
        this.metaAhorroId = metaAhorroId;
    }

    public Integer getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(Integer numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(LocalDate fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public BigDecimal getMontoEsperado() {
        return montoEsperado;
    }

    public void setMontoEsperado(BigDecimal montoEsperado) {
        this.montoEsperado = montoEsperado;
    }

    public EstadoCuota getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuota estado) {
        this.estado = estado;
    }

    public Long getAhorroId() {
        return ahorroId;
    }

    public void setAhorroId(Long ahorroId) {
        this.ahorroId = ahorroId;
    }
}
