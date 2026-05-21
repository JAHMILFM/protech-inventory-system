package com.ferreteria.protech.controller;

import com.ferreteria.protech.dto.OrdenCompraDTO;
import com.ferreteria.protech.dto.ProductDTO;
import com.ferreteria.protech.service.ProveedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proveedor")
public class ProveedorRestController {

    private final ProveedorService proveedorService;

    public ProveedorRestController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping("/metricas")
    public ResponseEntity<Map<String, Object>> getMetricas(Authentication auth) {
        return ResponseEntity.ok(proveedorService.obtenerMetricas(auth.getName()));
    }

    @GetMapping("/ordenes")
    public ResponseEntity<List<OrdenCompraDTO>> getOrdenes(Authentication auth) {
        List<OrdenCompraDTO> dtos = proveedorService.obtenerOrdenes(auth.getName())
                .stream()
                .map(OrdenCompraDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/ordenes/{id}/estado")
    public ResponseEntity<?> actualizarEstadoOrden(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        try {
            proveedorService.actualizarEstadoOrden(auth.getName(), id, body.get("estado"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ProductDTO>> getProductos(Authentication auth) {
        List<ProductDTO> dtos = proveedorService.obtenerMisProductos(auth.getName())
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stock-critico")
    public ResponseEntity<List<ProductDTO>> getStockCritico(Authentication auth) {
        List<ProductDTO> dtos = proveedorService.obtenerStockCritico(auth.getName())
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
