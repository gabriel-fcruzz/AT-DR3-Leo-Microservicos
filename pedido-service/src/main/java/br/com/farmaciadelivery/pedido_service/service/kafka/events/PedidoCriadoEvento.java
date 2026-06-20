package br.com.farmaciadelivery.pedido_service.service.kafka.events;

import br.com.farmaciadelivery.pedido_service.domain.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento de domínio publicado quando um pedido é confirmado.
 * Nomeado no passado (fato ocorrido). O correlationId viaja DENTRO do evento
 * para correlacionar os logs do pedido-service e do catalogo-service.
 */
public record PedidoCriadoEvento(
        Long pedidoId,
        Long estabelecimentoId,
        BigDecimal precoTotal,
        List<ItemEvento> itens,
        String correlationId,
        LocalDateTime criadoEm
) {
    public static PedidoCriadoEvento fromPedido(Pedido pedido, String correlationId) {
        List<ItemEvento> itens = pedido.getItens().stream()
                .map(i -> new ItemEvento(i.getProdutoId(), i.getQuantidade()))
                .toList();
        return new PedidoCriadoEvento(
                pedido.getId(),
                pedido.getEstabelecimentoId(),
                pedido.getPrecoTotal(),
                itens,
                correlationId,
                pedido.getCriadoEm()
        );
    }
}
