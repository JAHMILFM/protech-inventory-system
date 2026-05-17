package com.ferreteria.protech.dto;

import com.ferreteria.protech.model.Category;

/**
 * DTO para transferencia de datos de Categoría.
 */
public class CategoryDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String icono;
    private Integer nivel;
    private String nombreNivel;
    private Boolean activo;
    private Long padreId;
    private String padreNombre;
    private String rutaCompleta;
    private int cantidadProductos;
    private int cantidadSubcategorias;

    public static CategoryDTO fromEntity(Category c) {
        CategoryDTO dto = new CategoryDTO();
        dto.id = c.getId();
        dto.nombre = c.getNombre();
        dto.descripcion = c.getDescripcion();
        dto.icono = c.getIcono();
        dto.nivel = c.getNivel();
        dto.nombreNivel = c.getNombreNivel();
        dto.activo = c.getActivo();
        if (c.getPadre() != null) {
            dto.padreId = c.getPadre().getId();
            dto.padreNombre = c.getPadre().getNombre();
        }
        dto.rutaCompleta = c.getRutaCompleta();
        dto.cantidadProductos = c.getProductos() != null ? c.getProductos().size() : 0;
        dto.cantidadSubcategorias = c.getHijos() != null ? c.getHijos().size() : 0;
        return dto;
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

    public String getNombreNivel() { return nombreNivel; }
    public void setNombreNivel(String nombreNivel) { this.nombreNivel = nombreNivel; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Long getPadreId() { return padreId; }
    public void setPadreId(Long padreId) { this.padreId = padreId; }

    public String getPadreNombre() { return padreNombre; }
    public void setPadreNombre(String padreNombre) { this.padreNombre = padreNombre; }

    public String getRutaCompleta() { return rutaCompleta; }
    public void setRutaCompleta(String rutaCompleta) { this.rutaCompleta = rutaCompleta; }

    public int getCantidadProductos() { return cantidadProductos; }
    public void setCantidadProductos(int cantidadProductos) { this.cantidadProductos = cantidadProductos; }

    public int getCantidadSubcategorias() { return cantidadSubcategorias; }
    public void setCantidadSubcategorias(int cantidadSubcategorias) { this.cantidadSubcategorias = cantidadSubcategorias; }
}
