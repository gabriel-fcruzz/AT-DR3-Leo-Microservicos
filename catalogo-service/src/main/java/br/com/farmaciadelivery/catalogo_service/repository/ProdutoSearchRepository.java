package br.com.farmaciadelivery.catalogo_service.repository;

import br.com.farmaciadelivery.catalogo_service.model.ProdutoDocumento;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoSearchRepository extends ElasticsearchRepository<ProdutoDocumento, String> {

    List<ProdutoDocumento> findByNomeContainingOrDescricaoContainingOrCategoriaContaining(
            String nome,
            String descricao,
            String categoria
    );
}