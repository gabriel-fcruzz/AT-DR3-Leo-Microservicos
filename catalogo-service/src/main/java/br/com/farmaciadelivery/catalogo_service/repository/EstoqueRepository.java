package br.com.farmaciadelivery.catalogo_service.repository;

import br.com.farmaciadelivery.catalogo_service.model.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    List<Estoque> findByEstabelecimentoId(Long estabelecimentoId);

    Optional<Estoque> findByEstabelecimentoIdAndProdutoId(Long estabelecimentoId, Long produtoId);
}