package com.techdam.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Proyecto {
    private int id;
    private String nombre;
    private BigDecimal presupuesto;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean activo;

    public Proyecto() {}

    public Proyecto(int id, String nombre, BigDecimal presupuesto, LocalDate fechaInicio, LocalDate fechaFin, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.presupuesto = presupuesto;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = activo;
    }

    // getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPresupuesto() { return presupuesto; }
    public void setPresupuesto(BigDecimal presupuesto) { this.presupuesto = presupuesto; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "Proyecto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", presupuesto=" + presupuesto +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", activo=" + activo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Proyecto)) return false;
        Proyecto proyecto = (Proyecto) o;
        return id == proyecto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}