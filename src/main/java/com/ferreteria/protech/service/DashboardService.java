package com.ferreteria.protech.service;

import com.ferreteria.protech.dto.DashboardDTO;
import com.ferreteria.protech.repository.CategoryRepository;
import com.ferreteria.protech.repository.ProductRepository;
import com.ferreteria.protech.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para métricas del Dashboard.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final KardexService kardexService;

    public DashboardService(ProductRepository productRepository,
                            CategoryRepository categoryRepository,
                            UserRepository userRepository,
                            KardexService kardexService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.kardexService = kardexService;
    }

    public DashboardDTO obtenerMetricas() {
        DashboardDTO dto = new DashboardDTO();
        dto.setTotalProductos(productRepository.count());
        dto.setProductosActivos(productRepository.contarActivos());
        dto.setStockCritico(productRepository.contarStockCritico());
        dto.setTotalCategorias(categoryRepository.count());
        dto.setTotalUsuarios(userRepository.count());
        dto.setMovimientosHoy(kardexService.contarMovimientosHoy());
        dto.setValorInventario(productRepository.valorInventarioCosto());
        dto.setValorInventarioVenta(productRepository.valorInventarioVenta());
        return dto;
    }
}
