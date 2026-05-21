package com.ferreteria.protech.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroPedido;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private User cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> detalles = new ArrayList<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    private String estado;
    
    @Column(name = "direccion_envio")
    private String direccionEnvio;
    
    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "fecha_expiracion_reserva")
    private LocalDateTime fechaExpiracionReserva;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @Column(name = "cantidad_productos")
    private int cantidadProductos;

    public int getCantidadProductos() {
        if (detalles != null && !detalles.isEmpty()) {
            return detalles.stream().mapToInt(PedidoDetalle::getCantidad).sum();
        }
        return cantidadProductos;
    }

    public void setCantidadProductos(int cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }
    public User getCliente() { return cliente; }
    public void setCliente(User cliente) { this.cliente = cliente; }
    public List<PedidoDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<PedidoDetalle> detalles) { this.detalles = detalles; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccionEnvio) { this.direccionEnvio = direccionEnvio; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getFechaExpiracionReserva() { return fechaExpiracionReserva; }
    public void setFechaExpiracionReserva(LocalDateTime fechaExpiracionReserva) { this.fechaExpiracionReserva = fechaExpiracionReserva; }
}
