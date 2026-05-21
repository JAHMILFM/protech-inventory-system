package com.ferreteria.protech.controller;

import com.ferreteria.protech.dto.KardexDTO;
import com.ferreteria.protech.repository.KardexRepository;
import com.ferreteria.protech.repository.ProductRepository;
import com.ferreteria.protech.service.KardexService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el módulo de Ventas del Operario (POS).
 * Expone endpoints para confirmar ventas (multi-ítem) y obtener
 * métricas del turno del operario en tiempo real.
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final KardexService kardexService;
    private final KardexRepository kardexRepository;
    private final ProductRepository productRepository;

    public VentaController(KardexService kardexService,
                           KardexRepository kardexRepository,
                           ProductRepository productRepository) {
        this.kardexService = kardexService;
        this.kardexRepository = kardexRepository;
        this.productRepository = productRepository;
    }

    /**
     * Confirma una venta completa desde el POS.
     * Recibe una lista de ítems, crea un Kardex SALIDA por cada uno
     * y retorna el resumen de la transacción.
     *
     * Body esperado:
     * {
     *   "cliente": "Juan López",          (opcional)
     *   "metodoPago": "EFECTIVO",
     *   "items": [
     *     { "productoId": 1, "cantidad": 2 },
     *     { "productoId": 5, "cantidad": 1 }
     *   ]
     * }
     */
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarVenta(@RequestBody Map<String, Object> datos,
                                            Authentication auth) {
        try {
            String cliente = datos.get("cliente") != null
                    ? datos.get("cliente").toString() : "Cliente General";
            String metodoPago = datos.get("metodoPago") != null
                    ? datos.get("metodoPago").toString() : "EFECTIVO";

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) datos.get("items");

            if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No se proporcionaron ítems en la venta"));
            }

            String docRef = "VTA-" + System.currentTimeMillis();
            String motivo = "Venta Mostrador - Cliente: " + cliente + " - Pago: " + metodoPago;

            List<KardexDTO> movimientos = new ArrayList<>();
            BigDecimal totalVenta = BigDecimal.ZERO;

            for (Map<String, Object> item : items) {
                Long productoId = Long.valueOf(item.get("productoId").toString());
                int cantidad = Integer.parseInt(item.get("cantidad").toString());

                KardexDTO kardex = kardexService.registrarSalida(
                        productoId, cantidad, docRef, motivo);
                movimientos.add(kardex);

                if (kardex.getPrecioUnitario() != null) {
                    totalVenta = totalVenta.add(
                            kardex.getPrecioUnitario().multiply(new BigDecimal(cantidad)));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "ventaId", docRef,
                    "cliente", cliente,
                    "metodoPago", metodoPago,
                    "totalItems", items.size(),
                    "totalVenta", totalVenta,
                    "movimientos", movimientos
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Devuelve métricas del turno actual para el mini-dashboard del operario.
     */
    @GetMapping("/dashboard-operario")
    public ResponseEntity<?> dashboardOperario() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();

        long ventasHoy = kardexRepository.findAll().stream()
                .filter(k -> k.getTipoMovimiento().name().equals("SALIDA"))
                .filter(k -> k.getFechaMovimiento() != null
                        && k.getFechaMovimiento().isAfter(inicioDia))
                .count();

        BigDecimal totalRecaudado = kardexRepository.findAll().stream()
                .filter(k -> k.getTipoMovimiento().name().equals("SALIDA"))
                .filter(k -> k.getFechaMovimiento() != null
                        && k.getFechaMovimiento().isAfter(inicioDia))
                .map(k -> k.getCostoTotal() != null ? k.getCostoTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long stockCritico = productRepository.contarStockCritico();

        long entradasHoy = kardexRepository.findAll().stream()
                .filter(k -> k.getTipoMovimiento().name().equals("ENTRADA"))
                .filter(k -> k.getFechaMovimiento() != null
                        && k.getFechaMovimiento().isAfter(inicioDia))
                .count();

        return ResponseEntity.ok(Map.of(
                "ventasHoy", ventasHoy,
                "totalRecaudado", totalRecaudado,
                "stockCritico", stockCritico,
                "entradasHoy", entradasHoy
        ));
    }
}
