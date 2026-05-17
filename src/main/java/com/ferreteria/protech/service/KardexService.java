package com.ferreteria.protech.service;

import com.ferreteria.protech.dto.KardexDTO;
import com.ferreteria.protech.model.Kardex;
import com.ferreteria.protech.model.Product;
import com.ferreteria.protech.repository.KardexRepository;
import com.ferreteria.protech.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Kardex para auditoría de inventario.
 */
@Service
@Transactional
public class KardexService {

    private final KardexRepository kardexRepository;
    private final ProductRepository productRepository;

    public KardexService(KardexRepository kardexRepository, ProductRepository productRepository) {
        this.kardexRepository = kardexRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<KardexDTO> listarTodos() {
        return kardexRepository.findAllByOrderByFechaMovimientoDesc()
                .stream()
                .map(KardexDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KardexDTO> listarPorProducto(Long productoId) {
        return kardexRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId)
                .stream()
                .map(KardexDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** Registrar una entrada de mercancía */
    public KardexDTO registrarEntrada(Long productoId, int cantidad, BigDecimal precioUnitario,
                                      String documentoRef, String proveedor, String motivo) {
        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        Product product = productRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        int stockAnterior = product.getStockActual();
        int stockNuevo = stockAnterior + cantidad;

        product.setStockActual(stockNuevo);
        productRepository.save(product);

        Kardex kardex = new Kardex();
        kardex.setProducto(product);
        kardex.setTipoMovimiento(Kardex.TipoMovimiento.ENTRADA);
        kardex.setCantidad(cantidad);
        kardex.setStockAnterior(stockAnterior);
        kardex.setStockNuevo(stockNuevo);
        kardex.setPrecioUnitario(precioUnitario);
        kardex.setCostoTotal(precioUnitario.multiply(new BigDecimal(cantidad)));
        kardex.setDocumentoReferencia(documentoRef);
        kardex.setProveedor(proveedor);
        kardex.setMotivo(motivo);

        return KardexDTO.fromEntity(kardexRepository.save(kardex));
    }

    /** Registrar una salida de mercancía */
    public KardexDTO registrarSalida(Long productoId, int cantidad, String documentoRef, String motivo) {
        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        Product product = productRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        int stockAnterior = product.getStockActual();
        int stockNuevo = stockAnterior - cantidad;

        // No permitir stock negativo
        if (stockNuevo < 0) {
            throw new RuntimeException("Stock insuficiente. Stock actual: " + stockAnterior +
                    ", Cantidad solicitada: " + cantidad);
        }

        product.setStockActual(stockNuevo);
        productRepository.save(product);

        Kardex kardex = new Kardex();
        kardex.setProducto(product);
        kardex.setTipoMovimiento(Kardex.TipoMovimiento.SALIDA);
        kardex.setCantidad(cantidad);
        kardex.setStockAnterior(stockAnterior);
        kardex.setStockNuevo(stockNuevo);
        kardex.setPrecioUnitario(product.getPrecioVenta());
        kardex.setCostoTotal(product.getPrecioVenta().multiply(new BigDecimal(cantidad)));
        kardex.setDocumentoReferencia(documentoRef);
        kardex.setMotivo(motivo);

        return KardexDTO.fromEntity(kardexRepository.save(kardex));
    }

    @Transactional(readOnly = true)
    public long contarMovimientosHoy() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        return kardexRepository.contarMovimientosDesde(inicioDia);
    }
}
