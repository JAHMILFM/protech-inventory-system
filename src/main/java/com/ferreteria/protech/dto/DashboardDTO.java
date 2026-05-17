package com.ferreteria.protech.dto;

/**
 * DTO para métricas del Dashboard.
 */
public class DashboardDTO {

    private long totalProductos;
    private long productosActivos;
    private long stockCritico;
    private long totalCategorias;
    private long totalUsuarios;
    private long movimientosHoy;
    private double valorInventario;
    private double valorInventarioVenta;

    // ── Getters y Setters ───────────────────────────

    public long getTotalProductos() { return totalProductos; }
    public void setTotalProductos(long totalProductos) { this.totalProductos = totalProductos; }

    public long getProductosActivos() { return productosActivos; }
    public void setProductosActivos(long productosActivos) { this.productosActivos = productosActivos; }

    public long getStockCritico() { return stockCritico; }
    public void setStockCritico(long stockCritico) { this.stockCritico = stockCritico; }

    public long getTotalCategorias() { return totalCategorias; }
    public void setTotalCategorias(long totalCategorias) { this.totalCategorias = totalCategorias; }

    public long getTotalUsuarios() { return totalUsuarios; }
    public void setTotalUsuarios(long totalUsuarios) { this.totalUsuarios = totalUsuarios; }

    public long getMovimientosHoy() { return movimientosHoy; }
    public void setMovimientosHoy(long movimientosHoy) { this.movimientosHoy = movimientosHoy; }

    public double getValorInventario() { return valorInventario; }
    public void setValorInventario(double valorInventario) { this.valorInventario = valorInventario; }

    public double getValorInventarioVenta() { return valorInventarioVenta; }
    public void setValorInventarioVenta(double valorInventarioVenta) { this.valorInventarioVenta = valorInventarioVenta; }
}
