package com.ferreteria.protech.controller;

import com.ferreteria.protech.dto.CategoryDTO;
import com.ferreteria.protech.model.Category;
import com.ferreteria.protech.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Categorías.
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> listarTodas() {
        return ResponseEntity.ok(categoryService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(categoryService.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<List<CategoryDTO>> listarPorNivel(@PathVariable Integer nivel) {
        return ResponseEntity.ok(categoryService.listarPorNivel(nivel));
    }

    @GetMapping("/{id}/hijos")
    public ResponseEntity<List<CategoryDTO>> listarHijos(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.listarHijos(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Category category) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.crear(category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody Category category) {
        try {
            return ResponseEntity.ok(categoryService.actualizar(id, category));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivar(@PathVariable Long id) {
        try {
            categoryService.desactivar(id);
            return ResponseEntity.ok(Map.of("message", "Categoría desactivada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
