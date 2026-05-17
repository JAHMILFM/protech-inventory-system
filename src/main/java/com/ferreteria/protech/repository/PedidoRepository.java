package com.ferreteria.protech.repository;

import com.ferreteria.protech.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
