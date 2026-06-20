package br.com.farmaciadelivery.pedido_service.service.kafka.events;

/** Item dentro do evento PedidoCriado. */
public record ItemEvento(
        Long produtoId,
        Integer quantidade
) {
}
