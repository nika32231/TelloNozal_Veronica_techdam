package com.techdam.service;

import com.techdam.config.DatabaseConfigPool;
import com.techdam.dao.impl.ProyectoDAOImpl;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class TransaccionesService {

    private final ProyectoDAOImpl proyectoDAO = new ProyectoDAOImpl();

    /**
     * Transferir presupuesto (commit/rollback)
     */
    public boolean transferirPresupuesto(int proyectoOrigenId, int proyectoDestinoId, BigDecimal monto) throws Exception {
        Connection conn = null;
        try {
            conn = DatabaseConfigPool.getConnection();
            conn.setAutoCommit(false);

            boolean restado = proyectoDAO.restarPresupuesto(conn, proyectoOrigenId, monto);
            if (!restado) {
                conn.rollback();
                return false;
            }
            boolean sumado = proyectoDAO.sumarPresupuesto(conn, proyectoDestinoId, monto);
            if (!sumado) {
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Compatibilidad hacia atrás: versión antigua con 2 argumentos
     * Por defecto elegimos abortOnMissing = true (si hay ids faltantes, no hace nada).
     * Si prefieres otro comportamiento, cambia el valor por defecto.
     */
    public boolean asignarEmpleadosBatch(int proyectoId, List<Integer> empleadoIds) {
        return asignarEmpleadosBatch(proyectoId, empleadoIds, true);
    }

    /**
     * Inserta asignaciones en batch validando previamente que los empleados existan.
     *
     * @param proyectoId     id del proyecto
     * @param empleadoIds    lista de ids de empleados a asignar
     * @param abortOnMissing si true, no hace nada y devuelve false si hay empleados faltantes;
     *                       si false, inserta sólo los empleados existentes
     * @return true si commit exitoso (o no había nada que insertar), false en caso de fallo
     */
    public boolean asignarEmpleadosBatch(int proyectoId, List<Integer> empleadoIds, boolean abortOnMissing) {
        if (empleadoIds == null || empleadoIds.isEmpty()) return true;

        // Normalizar y eliminar duplicados
        List<Integer> distinctIds = empleadoIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (distinctIds.isEmpty()) return true;

        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement psSel = null;
        String insertSql = "INSERT INTO asignaciones (empleado_id, proyecto_id, fecha_asignacion, horas_semana) VALUES (?, ?, CURDATE(), 20)";

        try {
            conn = DatabaseConfigPool.getConnection();
            // 1) Validar existencia de empleados con un SELECT ... IN (?, ?, ...)
            String placeholders = distinctIds.stream().map(i -> "?").collect(Collectors.joining(","));
            String selSql = "SELECT id FROM empleados WHERE id IN (" + placeholders + ")";
            psSel = conn.prepareStatement(selSql);
            for (int i = 0; i < distinctIds.size(); i++) {
                psSel.setInt(i + 1, distinctIds.get(i));
            }
            Set<Integer> existentes = new HashSet<>();
            try (ResultSet rs = psSel.executeQuery()) {
                while (rs.next()) {
                    existentes.add(rs.getInt("id"));
                }
            }

            // Detectar faltantes
            List<Integer> faltantes = distinctIds.stream().filter(id -> !existentes.contains(id)).collect(Collectors.toList());
            if (!faltantes.isEmpty()) {
                System.out.println("Empleados no encontrados: " + faltantes);
                if (abortOnMissing) {
                    // No hacemos nada si hay ids inválidos
                    return false;
                }
                // Si no abortamos, continuamos solo con los existentes
                distinctIds = distinctIds.stream().filter(existentes::contains).collect(Collectors.toList());
                if (distinctIds.isEmpty()) {
                    // No hay nada que insertar
                    return true;
                }
            }

            // 2) Ejecutar batch solo con los ids existentes
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(insertSql);
            for (Integer empId : distinctIds) {
                ps.setInt(1, empId);
                ps.setInt(2, proyectoId);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            return true;
        } catch (BatchUpdateException bue) {
            bue.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            if (psSel != null) try { psSel.close(); } catch (SQLException ignored) {}
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * Alternativa: inserta fila a fila y continua si alguna falla (no recomendado si necesitas atomicidad).
     * Devuelve una lista de ids que fallaron.
     */
    public List<Integer> asignarEmpleadosIgnorandoErrores(int proyectoId, List<Integer> empleadoIds) {
        List<Integer> fallidos = new ArrayList<>();
        if (empleadoIds == null || empleadoIds.isEmpty()) return fallidos;

        String sql = "INSERT INTO asignaciones (empleado_id, proyecto_id, fecha_asignacion, horas_semana) VALUES (?, ?, CURDATE(), 20)";
        try (Connection conn = DatabaseConfigPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Integer empId : empleadoIds) {
                try {
                    ps.setInt(1, empId);
                    ps.setInt(2, proyectoId);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    // registrar el fallo y continuar
                    fallidos.add(empId);
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fallidos;
    }
}