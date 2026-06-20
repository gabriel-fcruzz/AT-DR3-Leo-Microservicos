package br.com.farmaciadelivery.catalogo_service.service;

import br.com.farmaciadelivery.catalogo_service.exception.RecursoNaoEncontradoException;
import br.com.farmaciadelivery.catalogo_service.model.Estoque;
import br.com.farmaciadelivery.catalogo_service.repository.EstoqueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;

    public Estoque adicionarAoEstoque(Estoque estoque) {
        log.info("Adicionando ao estoque: estabelecimento={}, produto={}",
                estoque.getEstabelecimento().getId(),
                estoque.getProduto().getId());
        return estoqueRepository.save(estoque);
    }

    public List<Estoque> listarPorEstabelecimento(Long estabelecimentoId) {
        return estoqueRepository.findByEstabelecimentoId(estabelecimentoId);
    }

    public boolean verificarDisponibilidade(Long estabelecimentoId, Long produtoId, Integer quantidade) {
        return estoqueRepository
                .findByEstabelecimentoIdAndProdutoId(estabelecimentoId, produtoId)
                .map(estoque -> estoque.getQuantidade() >= quantidade)
                .orElse(false);
    }

    public void decrementarEstoque(Long estabelecimentoId, Long produtoId, Integer quantidade) {
        Estoque estoque = estoqueRepository
                .findByEstabelecimentoIdAndProdutoId(estabelecimentoId, produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Estoque não encontrado para estabelecimento " + estabelecimentoId +
                                " e produto " + produtoId));

        if (estoque.getQuantidade() < quantidade) {
            throw new IllegalArgumentException(
                    "Estoque insuficiente. Disponível: " + estoque.getQuantidade() +
                            ", solicitado: " + quantidade);
        }

        estoque.setQuantidade(estoque.getQuantidade() - quantidade);
        estoqueRepository.save(estoque);
        log.info("Estoque decrementado: produto={}, quantidade removida={}, restante={}",
                produtoId, quantidade, estoque.getQuantidade());
    }
}