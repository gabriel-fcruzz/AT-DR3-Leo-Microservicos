package br.com.farmaciadelivery.pedido_service.service;

/**
 * Abstração da comunicação com o catalogo-service.
 * Implementação real (Feign) em prod; mock em dev/default. Padrão do projeto do professor.
 */
public interface CatalogoService {

    boolean verificarDisponibilidade(Long estabelecimentoId, Long produtoId, Integer quantidade);

    void decrementarEstoque(Long estabelecimentoId, Long produtoId, Integer quantidade);
}
