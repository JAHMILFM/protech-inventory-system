package com.ferreteria.protech.dto;

import com.ferreteria.protech.model.Pedido;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class PedidoDTO {
    private Long id;
    private String numeroPedido;
    private String cliente;
    private int cantidadProductos;
    private BigDecimal total;
    private String estado;
    private String fecha;

    public static PedidoDTO fromEntity(Pedido p) {
        PedidoDTO dto = new PedidoDTO();
        dto.setId(p.getId());
        dto.setNumeroPedido(p.getNumeroPedido());
        dto.setCliente(p.getCliente() != null ? p.getCliente().getNombreCompleto() : "Cliente Desconocido");
        dto.setCantidadProductos(p.getCantidadProductos());
        dto.setTotal(p.getTotal());
        dto.setEstado(p.getEstado());
        if (p.getCreatedAt() != null) {
            dto.setFecha(p.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public int getCantidadProductos() { return cantidadProductos; }
    public void setCantidadProductos(int cantidadProductos) { this.cantidadProductos = cantidadProductos; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}
