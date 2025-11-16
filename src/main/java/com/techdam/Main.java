package com.techdam;

import com.techdam.config.DatabaseConfigPool;
import com.techdam.dao.impl.EmpleadoDAOImpl;
import com.techdam.dao.impl.ProyectoDAOImpl;
import com.techdam.model.Empleado;
import com.techdam.model.Proyecto;
import com.techdam.service.ProcedimientosService;
import com.techdam.service.TransaccionesService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        EmpleadoDAOImpl empDao = new EmpleadoDAOImpl();
        ProyectoDAOImpl projDao = new ProyectoDAOImpl();
        ProcedimientosService procService = new ProcedimientosService();
        TransaccionesService txService = new TransaccionesService();

        // CRUD Empleado: crear
        Empleado nuevo = new Empleado(0, "Prueba Usuario", new BigDecimal("1300.00"), "QA", true, LocalDate.now());
        int nuevoId = empDao.crear(nuevo);
        System.out.println("Empleado creado con id: " + nuevoId);

        // Obtener todos empleados
        System.out.println("Todos empleados:");
        empDao.obtenerTodos().forEach(System.out::println);

        // CRUD Proyecto: crear
        Proyecto p = new Proyecto(0, "Proyecto Demo", new BigDecimal("10000.00"), LocalDate.now(), LocalDate.now().plusMonths(6), true);
        int pid = projDao.crear(p);
        System.out.println("Proyecto creado id: " + pid);

        // Transacción: transferir presupuesto (Opción A)
        boolean transfer = txService.transferirPresupuesto(1, 2, new BigDecimal("1000.00"));
        System.out.println("Transferencia completada: " + transfer);

        // Procedimiento: actualizar salarios departamento
        int updated = procService.actualizarSalariosDepartamento("Desarrollo", 5.0);
        System.out.println("Empleados actualizados en Desarrollo: " + updated);

        // Crear asignaciones en batch usando commit/rollback (sin savepoints)
        // Si algún id provoca fallo (ej. FK inexistente) toda la operación será revertida.
        List<Integer> empleadosAAsignar = Arrays.asList(1, 2, 9999, 3); // 9999 provocará fallo y hará rollback de todo el batch
        boolean asignacionesOk = txService.asignarEmpleadosBatch(pid, empleadosAAsignar);
        System.out.println("Asignaciones batch completadas: " + asignacionesOk);

        // Invocar funcion contar_empleados_proyecto
        int cnt = procService.contarEmpleadosEnProyecto(1);
        System.out.println("Empleados distintos en proyecto 1: " + cnt);

        // Cerrar el pool antes de salir
        DatabaseConfigPool.close();
    }
}