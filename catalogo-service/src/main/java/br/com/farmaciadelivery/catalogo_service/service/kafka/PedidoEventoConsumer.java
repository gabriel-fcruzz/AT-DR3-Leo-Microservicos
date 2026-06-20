package br.com.farmaciadelivery.catalogo_service.service.kafka;

import br.com.farmaciadelivery.catalogo_service.metrics.CatalogoMetrics;
import br.com.farmaciadelivery.catalogo_service.service.EstoqueService;
import br.com.farmaciadelivery.catalogo_service.service.kafka.events.ItemEvento;
import br.com.farmaciadelivery.catalogo_service.service.kafka.events.PedidoCriadoEvento;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consome PedidoCriado e decrementa o estoque (parte assíncrona do TP2).
 *
 * Cuidados demonstrados:
 * - Idempotência: guarda os pedidoId já processados, para não decrementar duas vezes
 *   caso a mesma mensagem chegue de novo (ex.: consumer caiu antes de commitar o offset).
 *   (Em memória aqui, por simplicidade; em produção seria uma tabela persistente.)
 * - Erro no consumo: try/catch + log. Em erro, libera o pedidoId para permitir reprocesso.
 * - Consumer offline: o Kafka mantém a mensagem no tópico; ao voltar, lê a partir do offset.
 */
@Component
@RequiredArgsConstructor
public class PedidoEventoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventoConsumer.class);

    private final EstoqueService estoqueService;
    private final CatalogoMetrics catalogoMetrics;

    private final Set<Long> pedidosProcessados = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "pedidos.criados", groupId = "catalogo-service-group")
    public void consumir(PedidoCriadoEvento evento) {
        // Propaga o correlationId do evento para os logs (mesmo id do pedido-service).
        MDC.put("correlationId", evento.correlationId());
        try {
            log.info("Evento PedidoCriado recebido (pedidoId={})", evento.pedidoId());

            // Idempotência: ignora se já processamos este pedido.
            if (!pedidosProcessados.add(evento.pedidoId())) {
                log.warn("Pedido {} já processado — ignorando (idempotência)", evento.pedidoId());
                return;
            }

            try {
                for (ItemEvento item : evento.itens()) {
                    estoqueService.decrementarEstoque(
                            evento.estabelecimentoId(), item.produtoId(), item.quantidade());
                }
                catalogoMetrics.incrementarEventosConsumidos();
                log.info("Estoque decrementado para o pedido {}", evento.pedidoId());
            } catch (Exception e) {
                // Libera para permitir reprocessamento numa próxima entrega.
                pedidosProcessados.remove(evento.pedidoId());
                log.error("Erro ao processar PedidoCriado {}: {}", evento.pedidoId(), e.getMessage());
            }
        } finally {
            MDC.remove("correlationId");
        }
    }
}
