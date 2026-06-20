package br.com.farmaciadelivery.pedido_service.service.kafka;

import br.com.farmaciadelivery.pedido_service.service.kafka.events.PedidoCriadoEvento;

/** Abstração da publicação do evento. Impl real (Kafka) em prod; mock em dev/default. */
public interface PedidoEventoProducer {
    void publicar(PedidoCriadoEvento evento);
}
