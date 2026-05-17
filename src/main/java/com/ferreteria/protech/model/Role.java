package com.ferreteria.protech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Entidad Role - Define los tipos de acceso:
 * ADMIN, OPERARIO, PROVEEDOR, CLIENTE
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "nombre", unique = true, nullable = false, length = 30)
    private String nombre;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    // ── Constructores ───────────────────────────────

    public Role() {}

    public Role(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // ── Getters y Setters ───────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
