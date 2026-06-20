package br.com.farmaciadelivery.pedido_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoRequest(
        @NotNull Long estabelecimentoId,
        @NotEmpty(message = "O pedido deve ter pelo menos um item") @Valid List<ItemPedidoRequest> itens
) {
}
