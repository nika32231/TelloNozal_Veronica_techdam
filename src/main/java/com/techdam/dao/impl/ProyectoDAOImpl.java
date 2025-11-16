package com.techdam.dao.impl;

import com.techdam.config.DatabaseConfigPool;
import com.techdam.dao.ProyectoDAO;
import com.techdam.model.Proyecto;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProyectoDAOImpl implements ProyectoDAO {

    @Override
    public int crear(Proyecto proyecto) throws Exception {
        String sql = "INSERT INTO proyectos (nombre, presupuesto, fecha_inicio, fecha_fin, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfigPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, proyecto.getNombre());
            ps.setBigDecimal(2, proyecto.getPresupuesto() != null ? proyecto.getPresupuesto() : BigDecimal.ZERO);
            if (proyecto.getFechaInicio() != null) ps.setDate(3, Date.valueOf(proyecto.getFechaInicio()));
            else ps.setNull(3, Types.DATE);
            if (proyecto.getFechaFin() != null) ps.setDate(4, Date.valueOf(proyecto.getFechaFin()));
            else ps.setNull(4, Types.DATE);
            ps.setBoolean(5, proyecto.isActivo());
            int affected = ps.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                else return -1;
            }
        } catch (SQLIntegrityConstraintViolationException dup) {
            // Ya existe un proyecto con ese nombre (índice UNIQUE). Recuperamos su id y lo devolvemos.
            String sel = "SELECT id FROM proyectos WHERE nombre = ?";
            try (Connection conn = DatabaseConfigPool.getConnection();
                 PreparedStatement ps2 = conn.prepareStatement(sel)) {
                ps2.setString(1, proyecto.getNombre());
                try (ResultSet rs = ps2.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    } else {
                        return -1;
                    }
                }
            }
        }
    }

    @Override
    public List<Proyecto> obtenerTodos() throws Exception {
        String sql = "SELECT id, nombre, presupuesto, fecha_inicio, fecha_fin, activo FROM proyectos";
        List<Proyecto> list = new ArrayList<>();
        try (Connection conn = DatabaseConfigPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public Optional<Proyecto> obtenerPorId(int id) throws Exception {
        String sql = "SELECT id, nombre, presupuesto, fecha_inicio, fecha_fin, activo FROM proyectos WHERE id = ?";
        try (Connection conn = DatabaseConfigPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                else return Optional.empty();
            }
        }
    }

    @Override
    public boolean actualizar(Proyecto proyecto) throws Exception {
        String sql = "UPDATE proyectos SET nombre = ?, presupuesto = ?, fecha_inicio = ?, fecha_fin = ?, activo = ? WHERE id = ?";
        try (Connection conn = DatabaseConfigPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, proyecto.getNombre());
            ps.setBigDecimal(2, proyecto.getPresupuesto());
            if (proyecto.getFechaInicio() != null) ps.setDate(3, Date.valueOf(proyecto.getFechaInicio()));
            else ps.setNull(3, Types.DATE);
            if (proyecto.getFechaFin() != null) ps.setDate(4, Date.valueOf(proyecto.getFechaFin()));
            else ps.setNull(4, Types.DATE);
            ps.setBoolean(5, proyecto.isActivo());
            ps.setInt(6, proyecto.getId());
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    @Override
    public boolean eliminar(int id) throws Exception {
        String sql = "DELETE FROM proyectos WHERE id = ?";
        try (Connection conn = DatabaseConfigPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        }
    }

    @Override
    public boolean sumarPresupuesto(Connection conn, int proyectoId, BigDecimal monto) throws Exception {
        String sql = "UPDATE proyectos SET presupuesto = presupuesto + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, monto);
            ps.setInt(2, proyectoId);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    @Override
    public boolean restarPresupuesto(Connection conn, int proyectoId, BigDecimal monto) throws Exception {
        String sql = "UPDATE proyectos SET presupuesto = presupuesto - ? WHERE id = ? AND presupuesto >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, monto);
            ps.setInt(2, proyectoId);
            ps.setBigDecimal(3, monto);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    private Proyecto mapRow(ResultSet rs) throws SQLException {
        Proyecto p = new Proyecto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setPresupuesto(rs.getBigDecimal("presupuesto"));
        Date di = rs.getDate("fecha_inicio");
        if (di != null) p.setFechaInicio(di.toLocalDate());
        Date df = rs.getDate("fecha_fin");
        if (df != null) p.setFechaFin(df.toLocalDate());
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
}