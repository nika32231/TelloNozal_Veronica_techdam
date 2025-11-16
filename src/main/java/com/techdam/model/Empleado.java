package com.techdam.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Empleado {
    private int id;
    private String nombre;
    private BigDecimal salario;
    private String departamento;
    private boolean activo;
    private LocalDate fechaContratacion;

    public Empleado() {}

    public Empleado(int id, String nombre, BigDecimal salario, String departamento, boolean activo, LocalDate fechaContratacion) {
        this.id = id;
        this.nombre = nombre;
        this.salario = salario;
        this.departamento = departamento;
        this.activo = activo;
        this.fechaContratacion = fechaContratacion;
    }

    // getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    @Override
    public String toString() {
        return "Empleado{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", salario=" + salario +
                ", departamento='" + departamento + '\'' +
                ", activo=" + activo +
                ", fechaContratacion=" + fechaContratacion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Empleado)) return false;
        Empleado empleado = (Empleado) o;
        return id == empleado.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}