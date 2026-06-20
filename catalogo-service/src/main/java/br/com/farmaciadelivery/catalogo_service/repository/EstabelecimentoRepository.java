package br.com.farmaciadelivery.catalogo_service.repository;

import br.com.farmaciadelivery.catalogo_service.model.Estabelecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, Long> {
}