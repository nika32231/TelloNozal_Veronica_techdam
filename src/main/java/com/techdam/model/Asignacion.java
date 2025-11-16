package com.techdam.model;

import java.time.LocalDate;
import java.util.Objects;

public class Asignacion {
    private int id;
    private int empleadoId;
    private int proyectoId;
    private LocalDate fechaAsignacion;
    private int horasSemana;

    public Asignacion() {}

    public Asignacion(int id, int empleadoId, int proyectoId, LocalDate fechaAsignacion, int horasSemana) {
        this.id = id;
        this.empleadoId = empleadoId;
        this.proyectoId = proyectoId;
        this.fechaAsignacion = fechaAsignacion;
        this.horasSemana = horasSemana;
    }

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(int empleadoId) { this.empleadoId = empleadoId; }

    public int getProyectoId() { return proyectoId; }
    public void setProyectoId(int proyectoId) { this.proyectoId = proyectoId; }

    public LocalDate getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDate fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public int getHorasSemana() { return horasSemana; }
    public void setHorasSemana(int horasSemana) { this.horasSemana = horasSemana; }

    @Override
    public String toString() {
        return "Asignacion{" +
                "id=" + id +
                ", empleadoId=" + empleadoId +
                ", proyectoId=" + proyectoId +
                ", fechaAsignacion=" + fechaAsignacion +
                ", horasSemana=" + horasSemana +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asignacion)) return false;
        Asignacion that = (Asignacion) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}