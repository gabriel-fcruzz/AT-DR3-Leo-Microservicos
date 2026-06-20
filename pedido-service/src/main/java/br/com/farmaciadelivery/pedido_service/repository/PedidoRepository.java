package br.com.farmaciadelivery.pedido_service.repository;

import br.com.farmaciadelivery.pedido_service.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
