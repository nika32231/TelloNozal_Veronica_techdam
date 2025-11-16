package com.techdam.dao;

import com.techdam.model.Empleado;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface EmpleadoDAO {
    int crear(Empleado empleado) throws Exception;
    List<Empleado> obtenerTodos() throws Exception;
    Optional<Empleado> obtenerPorId(int id) throws Exception;
    boolean actualizar(Empleado empleado) throws Exception;
    boolean eliminar(int id) throws Exception;

    // variante con Connection para uso en transacciones si es necesario
    int crear(Connection conn, Empleado empleado) throws Exception;
}