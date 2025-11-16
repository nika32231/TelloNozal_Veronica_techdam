package com.techdam.dao.impl;

import com.techdam.config.DatabaseConfigPool;
import com.techdam.dao.EmpleadoDAO;
import com.techdam.model.Empleado;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmpleadoDAOImpl implements EmpleadoDAO {

    @Override
    public int crear(Empleado empleado) throws Exception {
        try (Connection conn = DatabaseConfigPool.getConnection()) {
            return crear(conn, empleado);
        }
    }

    @Override
    public int crear(Connection conn, Empleado empleado) throws Exception {
        String sql = "INSERT INTO empleados (nombre, salario, departamento, activo, fecha_contratacion) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, empleado.getNombre());
            ps.setBigDecimal(2, empleado.getSalario() != null ? empleado.getSalario() : BigDecimal.ZERO);
            ps.setString(3, empleado.getDepartamento());
            ps.setBoolean(4, empleado.isActivo());
            if (empleado.getFechaContratacion() != null)
                ps.setDate(5, Date.valueOf(empleado.getFechaContratacion()));
            else
                ps.setNull(5, Types.DATE);

            int affected = ps.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return -1;
                }
            }
        } catch (SQLIntegrityConstraintViolationException dup) {
            // Ya existe un empleado con ese nombre (índice UNIQUE). Recuperamos su id y lo devolvemos.
            String sel = "SELECT id FROM empleados WHERE nombre = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(sel)) {
                ps2.setString(1, empleado.getNombre());
                try (ResultSet rs = ps2.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    } else {
                        // raro: constraint violado pero no encontramos el registro
                        return -1;
                    }
                }
            }
        }
    }

    @Override
    public List<Empleado> obtenerTodos() throws Exception {
        String sql = "SELECT id, nombre, salario, departamento, activo, fecha_contratacion FROM empleados";
        List<Empleado> list = new ArrayList<>();
        try (Connection conn = DatabaseConfigPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Empleado e = mapRow(rs);
                list.add(e);
            }
        }
        return list;
    }

    @Override
    public Optional<Empleado> obtenerPorId(int id) throws Exception {
        String sql = "SELECT id, nombre, salario, departamento, activo, fecha_contratacion FROM empleados WHERE id = ?";
        try (Connection conn = DatabaseConfigPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    @Override
    public boolean actualizar(Empleado empleado) throws Exception {
        String sql = "UPDATE empleados SET nombre = ?, salario = ?, departamento = ?, activo = ?, fecha_contratacion = ? WHERE id = ?";
        try (Connection conn = DatabaseConfigPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empleado.getNombre());
            ps.setBigDecimal(2, empleado.getSalario());
            ps.setString(3, empleado.getDepartamento());
            ps.setBoolean(4, empleado.isActivo());
            if (empleado.getFechaContratacion() != null)
                ps.setDate(5, Date.valueOf(empleado.getFechaContratacion()));
            else
                ps.setNull(5, Types.DATE);
            ps.setInt(6, empleado.getId());
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws Exception {
        String sql = "DELETE FROM empleados WHERE id = ?";
        try (Connection conn = DatabaseConfigPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        }
    }

    private Empleado mapRow(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setId(rs.getInt("id"));
        e.setNombre(rs.getString("nombre"));
        e.setSalario(rs.getBigDecimal("salario"));
        e.setDepartamento(rs.getString("departamento"));
        e.setActivo(rs.getBoolean("activo"));
        Date d = rs.getDate("fecha_contratacion");
        if (d != null) e.setFechaContratacion(d.toLocalDate());
        return e;
    }
}