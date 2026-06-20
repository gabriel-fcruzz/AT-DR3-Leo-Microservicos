package br.com.farmaciadelivery.pedido_service.service.kafka;

import br.com.farmaciadelivery.pedido_service.service.kafka.events.PedidoCriadoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Mock: não publica nada (dev/default, sem Kafka no ar). */
@Service
@Profile({"dev", "default"})
public class PedidoEventoProducerMock implements PedidoEventoProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventoProducerMock.class);

    @Override
    public void publicar(PedidoCriadoEvento evento) {
        log.info("[MOCK] PedidoCriado não publicado (pedidoId={})", evento.pedidoId());
    }
}
