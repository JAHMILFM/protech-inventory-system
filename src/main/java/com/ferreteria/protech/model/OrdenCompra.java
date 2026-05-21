package com.ferreteria.protech.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad para representar las Órdenes de Compra (B2B)
 * que la ferretería hace a los proveedores.
 */
@Entity
@Table(name = "ordenes_compra")
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_orden", unique = true, nullable = false)
    private String numeroOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private User proveedor;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenCompraDetalle> detalles = new ArrayList<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    // Estados típicos: PENDIENTE, EN_TRANSITO, RECIBIDO, CANCELADO
    @Column(nullable = false)
    private String estado = "PENDIENTE";

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

    // Calcular el total y cantidad automáticamente
    public int getCantidadArticulos() {
        if (detalles == null) return 0;
        return detalles.stream().mapToInt(OrdenCompraDetalle::getCantidad).sum();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }
    public User getProveedor() { return proveedor; }
    public void setProveedor(User proveedor) { this.proveedor = proveedor; }
    public List<OrdenCompraDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<OrdenCompraDetalle> detalles) { this.detalles = detalles; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
