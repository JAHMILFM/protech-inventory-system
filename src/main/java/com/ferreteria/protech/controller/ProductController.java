package com.ferreteria.protech.controller;

import com.ferreteria.protech.dto.ProductDTO;
import com.ferreteria.protech.model.Category;
import com.ferreteria.protech.model.Product;
import com.ferreteria.protech.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Productos.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> listarTodos() {
        return ResponseEntity.ok(productService.listarTodos());
    }

    @GetMapping("/todos")
    public ResponseEntity<List<ProductDTO>> listarTodosIncluyendoInactivos() {
        return ResponseEntity.ok(productService.listarTodosIncluyendoInactivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(e.getMessage()));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductDTO>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(productService.buscar(q));
    }

    @GetMapping("/stock-critico")
    public ResponseEntity<List<ProductDTO>> stockCritico() {
        return ResponseEntity.ok(productService.stockCritico());
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Product product) {
        try {
            ProductDTO created = productService.crear(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Product product) {
        try {
            return ResponseEntity.ok(productService.actualizar(id, product));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivar(@PathVariable Long id) {
        try {
            productService.desactivar(id);
            return ResponseEntity.ok(successResponse("Producto desactivado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<?> reactivar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.reactivar(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    // ── Helpers ─────────────────────────────────────

    private Map<String, String> errorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> successResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
