package br.com.farmaciadelivery.pedido_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemPedidoRequest(
        @NotNull Long produtoId,
        @NotNull @Min(value = 1, message = "Quantidade deve ser pelo menos 1") Integer quantidade,
        @NotNull BigDecimal precoUnitario
) {
}
