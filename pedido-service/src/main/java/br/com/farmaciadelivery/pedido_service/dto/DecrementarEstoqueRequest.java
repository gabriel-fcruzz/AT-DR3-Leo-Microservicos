package br.com.farmaciadelivery.pedido_service.dto;

/**
 * Corpo enviado ao catalogo-service em POST /estoque/decrementar.
 * Os campos espelham o DecrementarEstoqueDTO do catalogo-service.
 */
public record DecrementarEstoqueRequest(
        Long estabelecimentoId,
        Long produtoId,
        Integer quantidade
) {
}
