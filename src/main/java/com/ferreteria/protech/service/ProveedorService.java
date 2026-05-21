package com.ferreteria.protech.service;

import com.ferreteria.protech.model.OrdenCompra;
import com.ferreteria.protech.model.Product;
import com.ferreteria.protech.model.User;
import com.ferreteria.protech.repository.OrdenCompraRepository;
import com.ferreteria.protech.repository.ProductRepository;
import com.ferreteria.protech.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProveedorService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProveedorService(OrdenCompraRepository ordenCompraRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<OrdenCompra> obtenerOrdenes(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<OrdenCompra> ordenes = ordenCompraRepository.findByProveedorIdOrderByCreatedAtDesc(user.getId());
        // Forzar inicialización de detalles y productos dentro de la transacción
        ordenes.forEach(o -> {
            o.getDetalles().forEach(d -> {
                if (d.getProducto() != null) {
                    d.getProducto().getNombre(); // Force init
                }
            });
        });
        return ordenes;
    }

    @Transactional
    public void actualizarEstadoOrden(String username, Long ordenId, String nuevoEstado) {
        User user = userRepository.findByUsername(username).orElseThrow();
        OrdenCompra orden = ordenCompraRepository.findById(ordenId).orElseThrow();
        if (!orden.getProveedor().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para actualizar esta orden");
        }
        orden.setEstado(nuevoEstado);
        ordenCompraRepository.save(orden);
    }

    public List<Product> obtenerMisProductos(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return productRepository.findByProveedorId(user.getId());
    }

    public List<Product> obtenerStockCritico(String username) {
        return obtenerMisProductos(username).stream()
                .filter(p -> p.getStockActual() != null && p.getStockMinimo() != null 
                             && p.getStockActual() <= p.getStockMinimo())
                .collect(Collectors.toList());
    }

    public Map<String, Object> obtenerMetricas(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<OrdenCompra> ordenes = ordenCompraRepository.findByProveedorIdOrderByCreatedAtDesc(user.getId());

        long pendientes = ordenes.stream().filter(o -> "PENDIENTE".equals(o.getEstado())).count();
        long enTransito = ordenes.stream().filter(o -> "EN_TRANSITO".equals(o.getEstado())).count();
        double totalFacturado = ordenes.stream()
                .filter(o -> "RECIBIDO".equals(o.getEstado()) || "COMPLETADA".equals(o.getEstado()))
                .mapToDouble(o -> o.getTotal() != null ? o.getTotal().doubleValue() : 0.0)
                .sum();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("pendientes", pendientes);
        metrics.put("enTransito", enTransito);
        metrics.put("totalFacturado", totalFacturado);
        metrics.put("empresa", user.getEmpresaRepresentada());
        return metrics;
    }
}
