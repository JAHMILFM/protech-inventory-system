package com.ferreteria.protech.dto;

import com.ferreteria.protech.model.OrdenCompra;
import com.ferreteria.protech.model.OrdenCompraDetalle;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para OrdenCompra que expone los detalles de productos
 * sin exponer datos sensibles como passwords del User.
 */
public class OrdenCompraDTO {
    private Long id;
    private String numeroOrden;
    private String proveedorNombre;
    private int cantidadArticulos;
    private BigDecimal total;
    private String estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DetalleDTO> detalles;

    public static OrdenCompraDTO fromEntity(OrdenCompra oc) {
        OrdenCompraDTO dto = new OrdenCompraDTO();
        dto.setId(oc.getId());
        dto.setNumeroOrden(oc.getNumeroOrden());
        dto.setCantidadArticulos(oc.getCantidadArticulos());
        dto.setTotal(oc.getTotal());
        dto.setEstado(oc.getEstado());
        dto.setCreatedAt(oc.getCreatedAt());
        dto.setUpdatedAt(oc.getUpdatedAt());
        if (oc.getProveedor() != null) {
            dto.setProveedorNombre(oc.getProveedor().getNombreCompleto());
        }
        List<DetalleDTO> dets = new ArrayList<>();
        if (oc.getDetalles() != null) {
            for (OrdenCompraDetalle d : oc.getDetalles()) {
                dets.add(DetalleDTO.fromEntity(d));
            }
        }
        dto.setDetalles(dets);
        return dto;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }
    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }
    public int getCantidadArticulos() { return cantidadArticulos; }
    public void setCantidadArticulos(int cantidadArticulos) { this.cantidadArticulos = cantidadArticulos; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<DetalleDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleDTO> detalles) { this.detalles = detalles; }

    /**
     * Sub-DTO para los detalles (line items) de la orden.
     */
    public static class DetalleDTO {
        private Long id;
        private String productoSku;
        private String productoNombre;
        private int cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        public static DetalleDTO fromEntity(OrdenCompraDetalle d) {
            DetalleDTO dto = new DetalleDTO();
            dto.setId(d.getId());
            dto.setCantidad(d.getCantidad());
            dto.setPrecioUnitario(d.getPrecioUnitario());
            dto.setSubtotal(d.getSubtotal());
            if (d.getProducto() != null) {
                dto.setProductoSku(d.getProducto().getSku());
                dto.setProductoNombre(d.getProducto().getNombre());
            }
            return dto;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getProductoSku() { return productoSku; }
        public void setProductoSku(String productoSku) { this.productoSku = productoSku; }
        public String getProductoNombre() { return productoNombre; }
        public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
}
