package com.ferreteria.protech.repository;

import com.ferreteria.protech.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByEan13(String ean13);

    List<Product> findByActivoTrue();

    List<Product> findByActivoTrueOrderByNombreAsc();

    List<Product> findByCategoriaId(Long categoriaId);

    @Query("SELECT p FROM Product p WHERE p.activo = true AND p.stockActual <= p.stockMinimo")
    List<Product> findProductosStockCritico();

    @Query("SELECT p FROM Product p WHERE p.activo = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.marca) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> buscarProductos(@Param("query") String query);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.activo = true")
    long contarActivos();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.activo = true AND p.stockActual <= p.stockMinimo")
    long contarStockCritico();

    @Query("SELECT COALESCE(SUM(p.precioCosto * p.stockActual), 0) FROM Product p WHERE p.activo = true")
    double valorInventarioCosto();

    @Query("SELECT COALESCE(SUM(p.precioVenta * p.stockActual), 0) FROM Product p WHERE p.activo = true")
    double valorInventarioVenta();

    boolean existsBySku(String sku);
}
