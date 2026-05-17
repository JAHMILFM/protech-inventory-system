package com.ferreteria.protech.dto;

import com.ferreteria.protech.model.Kardex;

import java.math.BigDecimal;

/**
 * DTO para transferencia de datos de Kardex.
 */
public class KardexDTO {

    private Long id;
    private Long productoId;
    private String productoNombre;
    private String productoSku;
    private String tipoMovimiento;
    private String tipoMovimientoDescripcion;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private BigDecimal precioUnitario;
    private BigDecimal costoTotal;
    private String documentoReferencia;
    private String motivo;
    private String proveedor;
    private String usuarioNombre;
    private String fechaMovimiento;

    public static KardexDTO fromEntity(Kardex k) {
        KardexDTO dto = new KardexDTO();
        dto.id = k.getId();
        if (k.getProducto() != null) {
            dto.productoId = k.getProducto().getId();
            dto.productoNombre = k.getProducto().getNombre();
            dto.productoSku = k.getProducto().getSku();
        }
        dto.tipoMovimiento = k.getTipoMovimiento().name();
        dto.tipoMovimientoDescripcion = k.getTipoMovimiento().getDescripcion();
        dto.cantidad = k.getCantidad();
        dto.stockAnterior = k.getStockAnterior();
        dto.stockNuevo = k.getStockNuevo();
        dto.precioUnitario = k.getPrecioUnitario();
        dto.costoTotal = k.getCostoTotal();
        dto.documentoReferencia = k.getDocumentoReferencia();
        dto.motivo = k.getMotivo();
        dto.proveedor = k.getProveedor();
        if (k.getUsuario() != null) {
            dto.usuarioNombre = k.getUsuario().getNombreCompleto();
        }
        dto.fechaMovimiento = k.getFechaMovimiento() != null ? k.getFechaMovimiento().toString() : null;
        return dto;
    }

    // ── Getters y Setters ───────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getProductoSku() { return productoSku; }
    public void setProductoSku(String productoSku) { this.productoSku = productoSku; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public String getTipoMovimientoDescripcion() { return tipoMovimientoDescripcion; }
    public void setTipoMovimientoDescripcion(String tipoMovimientoDescripcion) { this.tipoMovimientoDescripcion = tipoMovimientoDescripcion; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Integer getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(Integer stockAnterior) { this.stockAnterior = stockAnterior; }

    public Integer getStockNuevo() { return stockNuevo; }
    public void setStockNuevo(Integer stockNuevo) { this.stockNuevo = stockNuevo; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

    public String getDocumentoReferencia() { return documentoReferencia; }
    public void setDocumentoReferencia(String documentoReferencia) { this.documentoReferencia = documentoReferencia; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(String fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
}
