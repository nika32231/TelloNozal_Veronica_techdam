package com.techdam.service;

import com.techdam.config.DatabaseConfigPool;

import java.math.BigDecimal;
import java.sql.*;

public class ProcedimientosService {

    /**
     * Invoca el procedimiento actualizar_salario_departamento
     * Devuelve número de empleados actualizados (OUT parameter)
     */
    public int actualizarSalariosDepartamento(String departamento, double porcentaje) {
        String call = "{call actualizar_salario_departamento(?, ?, ?)}";
        try (Connection conn = DatabaseConfigPool.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.setString(1, departamento);
            cstmt.setBigDecimal(2, BigDecimal.valueOf(porcentaje));
            cstmt.registerOutParameter(3, Types.INTEGER);
            cstmt.execute();
            return cstmt.getInt(3);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Invoca el procedimiento crear_asignacion
     * Devuelve id de asignacion (OUT) o 0 si fallo
     */
    public int crearAsignacion(int empleadoId, int proyectoId, Date fecha, int horas) {
        String call = "{call crear_asignacion(?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConfigPool.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.setInt(1, empleadoId);
            cstmt.setInt(2, proyectoId);
            cstmt.setDate(3, fecha);
            cstmt.setInt(4, horas);
            cstmt.registerOutParameter(5, Types.INTEGER);
            cstmt.execute();
            return cstmt.getInt(5);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Invoca la función contar_empleados_proyecto via SELECT
     */
    public int contarEmpleadosEnProyecto(int proyectoId) {
        String sql = "SELECT contar_empleados_proyecto(?) AS cnt";
        try (Connection conn = DatabaseConfigPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, proyectoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}