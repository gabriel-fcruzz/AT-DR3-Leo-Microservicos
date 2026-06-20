package br.com.farmaciadelivery.pedido_service.service.kafka;

import br.com.farmaciadelivery.pedido_service.service.kafka.events.PedidoCriadoEvento;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Publica o evento no tópico pedidos.criados. A key é o pedidoId
 * (garante ordem por pedido na partição). Ativa só no profile prod.
 */
@Service
@Profile("prod")
@RequiredArgsConstructor
public class PedidoEventoProducerImpl implements PedidoEventoProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventoProducerImpl.class);

    private final KafkaTemplate<String, PedidoCriadoEvento> kafkaTemplate;

    @Override
    public void publicar(PedidoCriadoEvento evento) {
        try {
            kafkaTemplate.send(KafkaConfig.TOPICO_PEDIDOS_CRIADOS,
                    String.valueOf(evento.pedidoId()), evento);
            log.info("Evento PedidoCriado publicado (pedidoId={}, correlationId={})",
                    evento.pedidoId(), evento.correlationId());
        } catch (Exception e) {
            // Falha do Kafka NÃO cancela o pedido (já confirmado). Só registramos.
            log.error("Falha ao publicar PedidoCriado (pedidoId={}): {}", evento.pedidoId(), e.getMessage());
        }
    }
}
