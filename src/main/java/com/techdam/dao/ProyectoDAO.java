package com.techdam.dao;

import com.techdam.model.Proyecto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface ProyectoDAO {
    int crear(Proyecto proyecto) throws Exception;
    List<Proyecto> obtenerTodos() throws Exception;
    Optional<Proyecto> obtenerPorId(int id) throws Exception;
    boolean actualizar(Proyecto proyecto) throws Exception;
    boolean eliminar(int id) throws Exception;

    // Métodos adicionales para transacciones que aceptan Connection
    boolean sumarPresupuesto(Connection conn, int proyectoId, BigDecimal monto) throws Exception;
    boolean restarPresupuesto(Connection conn, int proyectoId, BigDecimal monto) throws Exception;
}