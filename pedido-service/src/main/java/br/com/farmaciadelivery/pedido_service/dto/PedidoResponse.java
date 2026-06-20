package br.com.farmaciadelivery.pedido_service.dto;

import br.com.farmaciadelivery.pedido_service.domain.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long estabelecimentoId,
        String status,
        BigDecimal precoTotal,
        String statusPagamento,
        LocalDateTime criadoEm,
        List<ItemPedidoResponse> itens
) {
    public static PedidoResponse fromDomain(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens()
                .stream()
                .map(ItemPedidoResponse::fromDomain)
                .toList();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getEstabelecimentoId(),
                pedido.getStatus().name(),
                pedido.getPrecoTotal(),
                pedido.getStatusPagamento().name(),
                pedido.getCriadoEm(),
                itens
        );
    }
}
