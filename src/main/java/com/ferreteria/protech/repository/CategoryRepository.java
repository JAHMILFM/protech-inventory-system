package com.ferreteria.protech.repository;

import com.ferreteria.protech.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActivoTrue();

    List<Category> findByActivoTrueOrderByNombreAsc();

    List<Category> findByNivel(Integer nivel);

    List<Category> findByPadreId(Long padreId);

    List<Category> findByPadreIsNullAndActivoTrue();
}
