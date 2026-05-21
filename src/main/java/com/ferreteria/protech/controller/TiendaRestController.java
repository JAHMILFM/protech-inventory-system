package com.ferreteria.protech.controller;

import com.ferreteria.protech.dto.CheckoutRequest;
import com.ferreteria.protech.dto.ProductDTO;
import com.ferreteria.protech.model.Category;
import com.ferreteria.protech.service.TiendaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tienda")
public class TiendaRestController {

    private final TiendaService tiendaService;

    public TiendaRestController(TiendaService tiendaService) {
        this.tiendaService = tiendaService;
    }

    @GetMapping("/productos")
    public ResponseEntity<List<ProductDTO>> getProductos() {
        List<ProductDTO> dtos = tiendaService.obtenerProductosActivos()
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<Category>> getCategorias() {
        return ResponseEntity.ok(tiendaService.obtenerCategorias());
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request, Authentication auth) {
        try {
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "Debe iniciar sesión para comprar"));
            }
            com.ferreteria.protech.model.Pedido pedido = tiendaService.procesarCheckout(auth.getName(), request);
            return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "id", pedido.getId(), 
                    "numeroPedido", pedido.getNumeroPedido()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            String rootCause = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage() + " - Causa: " + rootCause));
        }
    }
}
