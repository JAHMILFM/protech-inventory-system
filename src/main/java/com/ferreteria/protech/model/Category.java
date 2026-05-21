package com.ferreteria.protech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Categoría - Estructura jerárquica:
 * Departamento > Categoría > Subcategoría
 */
@Entity
@Table(name = "categorias", indexes = {
    @Index(name = "idx_categoria_nombre", columnList = "nombre"),
    @Index(name = "idx_categoria_padre", columnList = "padre_id"),
    @Index(name = "idx_categoria_nivel", columnList = "nivel")
})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "icono", length = 50)
    private String icono;

    /**
     * Nivel jerárquico:
     * 1 = Departamento
     * 2 = Categoría
     * 3 = Subcategoría
     */
    @Column(name = "nivel", nullable = false)
    private Integer nivel = 1;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "padre_id")
    private Category padre;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "padre", fetch = FetchType.LAZY)
    private List<Category> hijos = new ArrayList<>();

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Product> productos = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Métodos de negocio ──────────────────────────

    /** Devuelve la ruta completa de la categoría (Dept > Cat > Subcat) */
    public String getRutaCompleta() {
        if (padre != null) {
            return padre.getRutaCompleta() + " > " + nombre;
        }
        return nombre;
    }

    /** Nombre del nivel jerárquico */
    public String getNombreNivel() {
        return switch (nivel) {
            case 1 -> "Departamento";
            case 2 -> "Categoría";
            case 3 -> "Subcategoría";
            default -> "Otro";
        };
    }

    // ── Getters y Setters ───────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }

    public Integer getNivel() { return nivel; }
    public void setNivel(Integer nivel) { this.nivel = nivel; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Category getPadre() { return padre; }
    public void setPadre(Category padre) { this.padre = padre; }

    @Transient
    public Long getParentId() {
        return padre != null ? padre.getId() : null;
    }

    public List<Category> getHijos() { return hijos; }
    public void setHijos(List<Category> hijos) { this.hijos = hijos; }

    public List<Product> getProductos() { return productos; }
    public void setProductos(List<Product> productos) { this.productos = productos; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
