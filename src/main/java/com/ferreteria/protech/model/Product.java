package com.ferreteria.protech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Producto - Representa un artículo de ferretería.
 * Incluye SKU único, EAN-13, precios (costo/venta/oferta),
 * stock (actual/mínimo/reserva), dimensiones, peso y ubicación física.
 */
@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_producto_sku", columnList = "sku", unique = true),
    @Index(name = "idx_producto_ean", columnList = "ean13"),
    @Index(name = "idx_producto_nombre", columnList = "nombre"),
    @Index(name = "idx_producto_categoria", columnList = "categoria_id"),
    @Index(name = "idx_producto_activo", columnList = "activo")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotBlank(message = "El SKU es obligatorio")
    @Column(name = "sku", unique = true, nullable = false, length = 20)
    private String sku;

    @Column(name = "ean13", length = 13)
    private String ean13;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El precio de costo es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de costo debe ser mayor a 0")
    @Column(name = "precio_costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCosto;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "precio_oferta", precision = 10, scale = 2)
    private BigDecimal precioOferta;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Min(value = 0)
    @Column(name = "stock_reserva")
    private Integer stockReserva = 0;

    @Column(name = "largo_cm", precision = 8, scale = 2)
    private BigDecimal largoCm;

    @Column(name = "ancho_cm", precision = 8, scale = 2)
    private BigDecimal anchoCm;

    @Column(name = "alto_cm", precision = 8, scale = 2)
    private BigDecimal altoCm;

    @Column(name = "peso_kg", precision = 8, scale = 3)
    private BigDecimal pesoKg;

    @Column(name = "ubicacion_pasillo", length = 10)
    private String ubicacionPasillo;

    @Column(name = "ubicacion_lado", length = 5)
    private String ubicacionLado;

    @Column(name = "ubicacion_nivel", length = 5)
    private String ubicacionNivel;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida = "UND";

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Category categoria;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private User proveedor;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Métodos de negocio ──────────────────────────

    /** Calcula el margen de ganancia en porcentaje */
    public BigDecimal getMargenGanancia() {
        if (precioCosto == null || precioCosto.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return precioVenta.subtract(precioCosto)
                .divide(precioCosto, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /** Calcula el precio con IGV (18%) */
    public BigDecimal getPrecioConIgv() {
        if (precioVenta == null) return BigDecimal.ZERO;
        return precioVenta.multiply(new BigDecimal("1.18"))
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /** Ubicación completa formateada */
    public String getUbicacionCompleta() {
        StringBuilder sb = new StringBuilder();
        if (ubicacionPasillo != null) sb.append("P").append(ubicacionPasillo);
        if (ubicacionLado != null) sb.append("-").append(ubicacionLado);
        if (ubicacionNivel != null) sb.append("-N").append(ubicacionNivel);
        return sb.length() > 0 ? sb.toString() : "Sin asignar";
    }

    /** Verifica si el stock está en nivel crítico */
    public boolean isStockCritico() {
        return stockActual != null && stockMinimo != null && stockActual <= stockMinimo;
    }

    // ── Getters y Setters ───────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getEan13() { return ean13; }
    public void setEan13(String ean13) { this.ean13 = ean13; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioCosto() { return precioCosto; }
    public void setPrecioCosto(BigDecimal precioCosto) { this.precioCosto = precioCosto; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public BigDecimal getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(BigDecimal precioOferta) { this.precioOferta = precioOferta; }

    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public Integer getStockReserva() { return stockReserva; }
    public void setStockReserva(Integer stockReserva) { this.stockReserva = stockReserva; }

    public BigDecimal getLargoCm() { return largoCm; }
    public void setLargoCm(BigDecimal largoCm) { this.largoCm = largoCm; }

    public BigDecimal getAnchoCm() { return anchoCm; }
    public void setAnchoCm(BigDecimal anchoCm) { this.anchoCm = anchoCm; }

    public BigDecimal getAltoCm() { return altoCm; }
    public void setAltoCm(BigDecimal altoCm) { this.altoCm = altoCm; }

    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }

    public String getUbicacionPasillo() { return ubicacionPasillo; }
    public void setUbicacionPasillo(String ubicacionPasillo) { this.ubicacionPasillo = ubicacionPasillo; }

    public String getUbicacionLado() { return ubicacionLado; }
    public void setUbicacionLado(String ubicacionLado) { this.ubicacionLado = ubicacionLado; }

    public String getUbicacionNivel() { return ubicacionNivel; }
    public void setUbicacionNivel(String ubicacionNivel) { this.ubicacionNivel = ubicacionNivel; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Category getCategoria() { return categoria; }
    public void setCategoria(Category categoria) { this.categoria = categoria; }

    public User getProveedor() { return proveedor; }
    public void setProveedor(User proveedor) { this.proveedor = proveedor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
