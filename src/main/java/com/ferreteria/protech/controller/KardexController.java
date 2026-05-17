package com.ferreteria.protech.controller;

import com.ferreteria.protech.dto.KardexDTO;
import com.ferreteria.protech.service.KardexService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Kardex (movimientos de inventario).
 */
@RestController
@RequestMapping("/api/kardex")
public class KardexController {

    private final KardexService kardexService;

    public KardexController(KardexService kardexService) {
        this.kardexService = kardexService;
    }

    @GetMapping
    public ResponseEntity<List<KardexDTO>> listarTodos() {
        return ResponseEntity.ok(kardexService.listarTodos());
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<KardexDTO>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(kardexService.listarPorProducto(productoId));
    }

    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(@RequestBody Map<String, Object> datos) {
        try {
            Long productoId = Long.valueOf(datos.get("productoId").toString());
            int cantidad = Integer.parseInt(datos.get("cantidad").toString());
            BigDecimal precioUnitario = new BigDecimal(datos.get("precioUnitario").toString());
            String documentoRef = datos.get("documentoReferencia") != null ?
                    datos.get("documentoReferencia").toString() : null;
            String proveedor = datos.get("proveedor") != null ?
                    datos.get("proveedor").toString() : null;
            String motivo = datos.get("motivo") != null ?
                    datos.get("motivo").toString() : "Entrada de mercancía";

            return ResponseEntity.ok(kardexService.registrarEntrada(
                    productoId, cantidad, precioUnitario, documentoRef, proveedor, motivo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/salida")
    public ResponseEntity<?> registrarSalida(@RequestBody Map<String, Object> datos) {
        try {
            Long productoId = Long.valueOf(datos.get("productoId").toString());
            int cantidad = Integer.parseInt(datos.get("cantidad").toString());
            String documentoRef = datos.get("documentoReferencia") != null ?
                    datos.get("documentoReferencia").toString() : null;
            String motivo = datos.get("motivo") != null ?
                    datos.get("motivo").toString() : "Salida de mercancía";

            return ResponseEntity.ok(kardexService.registrarSalida(
                    productoId, cantidad, documentoRef, motivo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
