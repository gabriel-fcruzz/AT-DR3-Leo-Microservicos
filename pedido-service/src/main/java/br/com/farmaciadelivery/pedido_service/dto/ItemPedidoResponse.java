package br.com.farmaciadelivery.pedido_service.dto;

import br.com.farmaciadelivery.pedido_service.domain.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        Long produtoId,
        Integer quantidade,
        BigDecimal precoUnitario
) {
    public static ItemPedidoResponse fromDomain(ItemPedido item) {
        return new ItemPedidoResponse(
                item.getProdutoId(),
                item.getQuantidade(),
                item.getPrecoUnitario()
        );
    }
}
