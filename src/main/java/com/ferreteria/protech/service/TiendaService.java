package com.ferreteria.protech.service;

import com.ferreteria.protech.dto.CheckoutRequest;
import com.ferreteria.protech.model.*;
import com.ferreteria.protech.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@Service
public class TiendaService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final PedidoRepository pedidoRepo;
    private final UserRepository userRepo;
    private final KardexRepository kardexRepo;

    public TiendaService(ProductRepository productRepo, CategoryRepository categoryRepo,
                         PedidoRepository pedidoRepo, UserRepository userRepo,
                         KardexRepository kardexRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.pedidoRepo = pedidoRepo;
        this.userRepo = userRepo;
        this.kardexRepo = kardexRepo;
    }

    public List<Product> obtenerProductosActivos() {
        return productRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()) && p.getStockActual() > 0)
                .toList();
    }

    public List<Category> obtenerCategorias() {
        return categoryRepo.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .toList();
    }

    @Transactional
    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 500)
    )
    public Pedido procesarCheckout(String username, CheckoutRequest request) {
        // Fase 1: Crear la reserva (Transaccional independiente si se maneja en otro bean, 
        // pero aquí Spring lo maneja por proxy si se llama desde el Controller)
        Pedido pedidoReservado = crearReservaCheckout(username, request);
        
        // Simulación de pasarela de pago exitosa (En un flujo real, esto ocurre en otro endpoint)
        // Fase 2: Confirmar el pago
        return confirmarPagoReserva(pedidoReservado.getId(), request);
    }

    @Transactional
    public Pedido crearReservaCheckout(String username, CheckoutRequest request) {
        User cliente = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        Pedido pedido = new Pedido();
        pedido.setNumeroPedido("WEB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pedido.setCliente(cliente);
        pedido.setEstado("RESERVADO");
        pedido.setFechaExpiracionReserva(LocalDateTime.now().plusMinutes(15));
        pedido.setDireccionEnvio(request.getDireccionEnvio() != null ? request.getDireccionEnvio() : "Recojo en Tienda");
        pedido.setMetodoPago(request.getMetodoPago() != null ? request.getMetodoPago() : "NO_ESPECIFICADO");
        pedido.setDetalles(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;

        for (CheckoutRequest.CartItem item : request.getItems()) {
            Product producto = productRepo.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

            // Validación Optimista: El stockActual debe soportar la cantidad solicitada
            if (producto.getStockActual() < item.getCantidad()) {
                throw new RuntimeException("El inventario ha cambiado, stock insuficiente para: " + producto.getNombre());
            }

            // Reserva temporal
            producto.setStockActual(producto.getStockActual() - item.getCantidad());
            producto.setStockReserva(producto.getStockReserva() + item.getCantidad());
            
            // Guardar para forzar incremento del @Version (Optimistic Locking)
            productRepo.save(producto);

            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(producto.getPrecioVenta().multiply(new BigDecimal(item.getCantidad())));

            pedido.getDetalles().add(detalle);
            total = total.add(detalle.getSubtotal());
        }

        pedido.setTotal(total);
        pedido.setCantidadProductos(request.getItems().stream().mapToInt(CheckoutRequest.CartItem::getCantidad).sum());
        return pedidoRepo.save(pedido);
    }

    @Transactional
    public Pedido confirmarPagoReserva(Long pedidoId, CheckoutRequest request) {
        Pedido pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (!"RESERVADO".equals(pedido.getEstado())) {
            throw new RuntimeException("El pedido no está en estado de reserva");
        }

        // Consolidar pago
        pedido.setEstado("PAGADO");
        pedido.setFechaExpiracionReserva(null); // Ya no expira

        for (PedidoDetalle detalle : pedido.getDetalles()) {
            Product producto = detalle.getProducto();
            
            // Retirar el stock de la reserva permanentemente
            producto.setStockReserva(producto.getStockReserva() - detalle.getCantidad());
            productRepo.save(producto);

            // Registrar movimiento contable (Salida definitiva en Kardex)
            Kardex kardex = new Kardex();
            kardex.setProducto(producto);
            kardex.setTipoMovimiento(Kardex.TipoMovimiento.SALIDA);
            kardex.setCantidad(detalle.getCantidad());
            // Nota: Para el Kardex, el stock anterior contable ya excluye la reserva
            kardex.setStockAnterior(producto.getStockActual() + detalle.getCantidad()); 
            kardex.setStockNuevo(producto.getStockActual());
            kardex.setPrecioUnitario(producto.getPrecioVenta());
            kardex.setCostoTotal(detalle.getSubtotal());
            kardex.setMotivo("Venta Web B2C Confirmada");
            kardex.setUsuario(pedido.getCliente());
            kardexRepo.save(kardex);
        }

        return pedidoRepo.save(pedido);
    }
}
