package com.ferreteria.protech.ai;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class InventoryDatabaseTools {

    private final JdbcTemplate jdbcTemplate;

    public InventoryDatabaseTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool("Ejecuta una consulta SQL SELECT en la base de datos de inventario. NO PUEDES usar UPDATE, DELETE, INSERT o DROP.")
    public String ejecutarConsultaSelect(String consultaSql) {
        // Validación estricta para evitar inyecciones destructivas
        String sqlLimpio = consultaSql.trim().toUpperCase();
        if (!sqlLimpio.startsWith("SELECT") || sqlLimpio.contains("DROP") || sqlLimpio.contains("DELETE") || 
            sqlLimpio.contains("UPDATE") || sqlLimpio.contains("INSERT") || sqlLimpio.contains("TRUNCATE") || 
            sqlLimpio.contains("ALTER")) {
            return "ERROR: Operación no permitida. Solo se pueden ejecutar consultas SELECT de lectura.";
        }

        try {
            // Se ejecuta la consulta y se devuelve el resultado convertido a String
            List<Map<String, Object>> resultados = jdbcTemplate.queryForList(consultaSql);
            if (resultados.isEmpty()) {
                return "La consulta no devolvió resultados.";
            }
            return resultados.toString();
        } catch (Exception e) {
            return "ERROR al ejecutar SQL: " + e.getMessage();
        }
    }

    @Tool("Obtiene el esquema de la base de datos (tablas y columnas) para saber qué campos existen en productos y kardex.")
    public String obtenerEsquemaBaseDatos() {
        return """
        Tablas principales:
        - productos: id, sku, ean13, nombre, descripcion, marca, modelo, precio_costo, precio_venta, stock_actual, stock_minimo, stock_reserva, ubicacion_pasillo, ubicacion_lado, ubicacion_nivel, categoria_id, activo, created_at
        - kardex: id, producto_id, fecha_movimiento, tipo_movimiento (ENTRADA, SALIDA), cantidad, stock_anterior, stock_nuevo, motivo, proveedor, usuario_id
        - categorias: id, nombre, descripcion, nivel, padre_id
        """;
    }
}
