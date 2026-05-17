package com.ferreteria.protech.repository;

import com.ferreteria.protech.model.Kardex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KardexRepository extends JpaRepository<Kardex, Long> {

    List<Kardex> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);

    List<Kardex> findAllByOrderByFechaMovimientoDesc();

    @Query("SELECT k FROM Kardex k WHERE k.fechaMovimiento BETWEEN :inicio AND :fin ORDER BY k.fechaMovimiento DESC")
    List<Kardex> findByRangoFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(k) FROM Kardex k WHERE k.fechaMovimiento >= :inicio")
    long contarMovimientosDesde(@Param("inicio") LocalDateTime inicio);
}
