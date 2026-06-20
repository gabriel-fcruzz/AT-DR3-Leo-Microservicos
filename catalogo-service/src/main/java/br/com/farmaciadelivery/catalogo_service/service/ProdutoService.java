package br.com.farmaciadelivery.catalogo_service.service;

import br.com.farmaciadelivery.catalogo_service.exception.RecursoNaoEncontradoException;
import br.com.farmaciadelivery.catalogo_service.model.Produto;
import br.com.farmaciadelivery.catalogo_service.model.ProdutoDocumento;
import br.com.farmaciadelivery.catalogo_service.repository.ProdutoRepository;
import br.com.farmaciadelivery.catalogo_service.repository.ProdutoSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoSearchRepository produtoSearchRepository;

    public Produto criar(Produto produto) {
        log.info("Criando produto: {}", produto.getNome());

        // 1. salva no PostgreSQL
        Produto salvo = produtoRepository.save(produto);

        // 2. indexa no Elasticsearch com o mesmo id
        ProdutoDocumento documento = new ProdutoDocumento();
        documento.setId(salvo.getId().toString());
        documento.setNome(salvo.getNome());
        documento.setDescricao(salvo.getDescricao());
        documento.setCategoria(salvo.getCategoria());
        documento.setFabricante(salvo.getFabricante());

        // O que acontece se o ES cair durante a criação?
        // O produto já foi salvo no PostgreSQL na linha anterior.
        // O ES vai falhar e lançar exceção — o produto existe
        // no banco mas não está indexado. Resolver com fila.

        produtoSearchRepository.save(documento);
        log.info("Produto indexado no Elasticsearch com id: {}", salvo.getId());

        return salvo;
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado com id: " + id));
    }

    public List<ProdutoDocumento> buscar(String termo) {
        log.info("Buscando produtos no Elasticsearch com termo: {}", termo);
        return produtoSearchRepository
                .findByNomeContainingOrDescricaoContainingOrCategoriaContaining(
                        termo, termo, termo);
    }
}