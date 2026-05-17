package com.ferreteria.protech.service;

import com.ferreteria.protech.dto.ProductDTO;
import com.ferreteria.protech.model.Category;
import com.ferreteria.protech.model.Kardex;
import com.ferreteria.protech.model.Product;
import com.ferreteria.protech.repository.CategoryRepository;
import com.ferreteria.protech.repository.KardexRepository;
import com.ferreteria.protech.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de Productos con validaciones de negocio.
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final KardexRepository kardexRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          KardexRepository kardexRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.kardexRepository = kardexRepository;
    }

    /** Lista todos los productos activos como DTOs */
    @Transactional(readOnly = true)
    public List<ProductDTO> listarTodos() {
        return productRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** Lista todos (incluidos inactivos) */
    @Transactional(readOnly = true)
    public List<ProductDTO> listarTodosIncluyendoInactivos() {
        return productRepository.findAll()
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** Obtiene un producto por ID */
    @Transactional(readOnly = true)
    public ProductDTO obtenerPorId(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return ProductDTO.fromEntity(product);
    }

    /** Busca productos por nombre, SKU o marca */
    @Transactional(readOnly = true)
    public List<ProductDTO> buscar(String query) {
        return productRepository.buscarProductos(query)
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** Productos con stock crítico */
    @Transactional(readOnly = true)
    public List<ProductDTO> stockCritico() {
        return productRepository.findProductosStockCritico()
                .stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** Crear producto nuevo */
    public ProductDTO crear(Product product) {
        // Validar SKU único
        if (productRepository.existsBySku(product.getSku())) {
            throw new RuntimeException("Ya existe un producto con el SKU: " + product.getSku());
        }

        // Validar precio de venta >= precio de costo
        if (product.getPrecioVenta().compareTo(product.getPrecioCosto()) < 0) {
            throw new RuntimeException("El precio de venta no puede ser menor al precio de costo");
        }

        // Asignar categoría si se envió
        if (product.getCategoria() != null && product.getCategoria().getId() != null) {
            Category cat = categoryRepository.findById(product.getCategoria().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            product.setCategoria(cat);
        }

        // Inicializar stock de reserva
        if (product.getStockReserva() == null) {
            product.setStockReserva(0);
        }

        Product saved = productRepository.save(product);

        // Registrar en kardex si hay stock inicial
        if (saved.getStockActual() > 0) {
            Kardex kardex = new Kardex();
            kardex.setProducto(saved);
            kardex.setTipoMovimiento(Kardex.TipoMovimiento.ENTRADA);
            kardex.setCantidad(saved.getStockActual());
            kardex.setStockAnterior(0);
            kardex.setStockNuevo(saved.getStockActual());
            kardex.setPrecioUnitario(saved.getPrecioCosto());
            kardex.setCostoTotal(saved.getPrecioCosto().multiply(new BigDecimal(saved.getStockActual())));
            kardex.setMotivo("Stock inicial al crear producto");
            kardexRepository.save(kardex);
        }

        return ProductDTO.fromEntity(saved);
    }

    /** Actualizar producto existente */
    public ProductDTO actualizar(Long id, Product datos) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        // Validar SKU único si cambió
        if (!product.getSku().equals(datos.getSku()) && productRepository.existsBySku(datos.getSku())) {
            throw new RuntimeException("Ya existe un producto con el SKU: " + datos.getSku());
        }

        // Validar precio de venta >= precio de costo
        if (datos.getPrecioVenta().compareTo(datos.getPrecioCosto()) < 0) {
            throw new RuntimeException("El precio de venta no puede ser menor al precio de costo");
        }

        // No permitir stock negativo
        if (datos.getStockActual() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        // Detectar cambio de stock y registrar en kardex
        int stockAnterior = product.getStockActual();
        int stockNuevo = datos.getStockActual();
        if (stockAnterior != stockNuevo) {
            Kardex kardex = new Kardex();
            kardex.setProducto(product);
            kardex.setStockAnterior(stockAnterior);
            kardex.setStockNuevo(stockNuevo);
            kardex.setCantidad(Math.abs(stockNuevo - stockAnterior));
            kardex.setPrecioUnitario(datos.getPrecioCosto());

            if (stockNuevo > stockAnterior) {
                kardex.setTipoMovimiento(Kardex.TipoMovimiento.AJUSTE_POSITIVO);
                kardex.setMotivo("Ajuste positivo por edición de producto");
            } else {
                kardex.setTipoMovimiento(Kardex.TipoMovimiento.AJUSTE_NEGATIVO);
                kardex.setMotivo("Ajuste negativo por edición de producto");
            }
            kardex.setCostoTotal(datos.getPrecioCosto().multiply(new BigDecimal(kardex.getCantidad())));
            kardexRepository.save(kardex);
        }

        // Actualizar campos
        product.setSku(datos.getSku());
        product.setEan13(datos.getEan13());
        product.setNombre(datos.getNombre());
        product.setDescripcion(datos.getDescripcion());
        product.setPrecioCosto(datos.getPrecioCosto());
        product.setPrecioVenta(datos.getPrecioVenta());
        product.setPrecioOferta(datos.getPrecioOferta());
        product.setStockActual(datos.getStockActual());
        product.setStockMinimo(datos.getStockMinimo());
        product.setStockReserva(datos.getStockReserva() != null ? datos.getStockReserva() : 0);
        product.setLargoCm(datos.getLargoCm());
        product.setAnchoCm(datos.getAnchoCm());
        product.setAltoCm(datos.getAltoCm());
        product.setPesoKg(datos.getPesoKg());
        product.setUbicacionPasillo(datos.getUbicacionPasillo());
        product.setUbicacionLado(datos.getUbicacionLado());
        product.setUbicacionNivel(datos.getUbicacionNivel());
        product.setMarca(datos.getMarca());
        product.setModelo(datos.getModelo());
        product.setUnidadMedida(datos.getUnidadMedida());
        product.setImagenUrl(datos.getImagenUrl());

        // Asignar categoría
        if (datos.getCategoria() != null && datos.getCategoria().getId() != null) {
            Category cat = categoryRepository.findById(datos.getCategoria().getId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            product.setCategoria(cat);
        } else {
            product.setCategoria(null);
        }

        return ProductDTO.fromEntity(productRepository.save(product));
    }

    /** Desactivar producto (no eliminar si está activo con stock) */
    public void desactivar(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        if (product.getActivo() && product.getStockActual() > 0) {
            throw new RuntimeException("No se puede desactivar un producto con stock disponible (" +
                    product.getStockActual() + " unidades). Reduzca el stock a 0 primero.");
        }

        product.setActivo(false);
        productRepository.save(product);

        // Registrar en Kardex
        Kardex kardex = new Kardex();
        kardex.setProducto(product);
        kardex.setTipoMovimiento(Kardex.TipoMovimiento.AJUSTE_NEGATIVO);
        kardex.setCantidad(0);
        kardex.setStockAnterior(product.getStockActual());
        kardex.setStockNuevo(product.getStockActual());
        kardex.setMotivo("Producto desactivado del sistema");
        kardexRepository.save(kardex);
    }

    /** Reactivar producto */
    public ProductDTO reactivar(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        product.setActivo(true);
        return ProductDTO.fromEntity(productRepository.save(product));
    }
}
