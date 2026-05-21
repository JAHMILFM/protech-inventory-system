package com.ferreteria.protech.scheduler;

import com.ferreteria.protech.model.Pedido;
import com.ferreteria.protech.model.PedidoDetalle;
import com.ferreteria.protech.model.Product;
import com.ferreteria.protech.repository.PedidoRepository;
import com.ferreteria.protech.repository.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StockReleaseScheduler {

    private final PedidoRepository pedidoRepo;
    private final ProductRepository productRepo;

    public StockReleaseScheduler(PedidoRepository pedidoRepo, ProductRepository productRepo) {
        this.pedidoRepo = pedidoRepo;
        this.productRepo = productRepo;
    }

    /**
     * Se ejecuta cada 1 minuto (60000 ms).
     * Busca pedidos en estado "RESERVADO" cuya fecha de expiración ya pasó
     * y libera el stock, devolviéndolo de reserva al actual.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void liberarStockExpirado() {
        LocalDateTime now = LocalDateTime.now();
        List<Pedido> pedidosExpirados = pedidoRepo.findAll().stream()
                .filter(p -> "RESERVADO".equals(p.getEstado()))
                .filter(p -> p.getFechaExpiracionReserva() != null && p.getFechaExpiracionReserva().isBefore(now))
                .toList();

        for (Pedido pedido : pedidosExpirados) {
            for (PedidoDetalle detalle : pedido.getDetalles()) {
                Product producto = detalle.getProducto();
                // Devolver la cantidad al stock actual y restarlo de la reserva
                producto.setStockActual(producto.getStockActual() + detalle.getCantidad());
                producto.setStockReserva(producto.getStockReserva() - detalle.getCantidad());
                productRepo.save(producto);
            }
            pedido.setEstado("CANCELADO_POR_TIEMPO");
            pedidoRepo.save(pedido);
            System.out.println("Scheduler: Pedido " + pedido.getNumeroPedido() + " expirado. Stock devuelto al anaquel.");
        }
    }
}
