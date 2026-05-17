package com.ferreteria.protech.service;

import com.ferreteria.protech.dto.CategoryDTO;
import com.ferreteria.protech.model.Category;
import com.ferreteria.protech.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Categorías con estructura jerárquica.
 */
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> listarTodas() {
        return categoryRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDTO obtenerPorId(Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
        return CategoryDTO.fromEntity(cat);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> listarPorNivel(Integer nivel) {
        return categoryRepository.findByNivel(nivel)
                .stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> listarHijos(Long padreId) {
        return categoryRepository.findByPadreId(padreId)
                .stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public CategoryDTO crear(Category category) {
        // Determinar nivel jerárquico automáticamente
        if (category.getPadre() != null && category.getPadre().getId() != null) {
            Category padre = categoryRepository.findById(category.getPadre().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría padre no encontrada"));
            category.setPadre(padre);
            category.setNivel(padre.getNivel() + 1);
        } else {
            category.setPadre(null);
            category.setNivel(1);
        }

        return CategoryDTO.fromEntity(categoryRepository.save(category));
    }

    public CategoryDTO actualizar(Long id, Category datos) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        category.setNombre(datos.getNombre());
        category.setDescripcion(datos.getDescripcion());
        category.setIcono(datos.getIcono());

        if (datos.getPadre() != null && datos.getPadre().getId() != null) {
            Category padre = categoryRepository.findById(datos.getPadre().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría padre no encontrada"));
            category.setPadre(padre);
            category.setNivel(padre.getNivel() + 1);
        }

        return CategoryDTO.fromEntity(categoryRepository.save(category));
    }

    public void desactivar(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        if (category.getProductos() != null && !category.getProductos().isEmpty()) {
            throw new RuntimeException("No se puede desactivar una categoría con productos asociados");
        }

        category.setActivo(false);
        categoryRepository.save(category);
    }
}
