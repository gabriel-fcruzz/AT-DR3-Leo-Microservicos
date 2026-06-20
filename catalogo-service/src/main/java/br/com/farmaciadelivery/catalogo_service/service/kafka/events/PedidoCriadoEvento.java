package br.com.farmaciadelivery.catalogo_service.service.kafka.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cópia local do evento publicado pelo pedido-service (mesmos nomes de campo,
 * para o Jackson desserializar). Cada serviço tem sua cópia (estilo do professor).
 */
public record PedidoCriadoEvento(
        Long pedidoId,
        Long estabelecimentoId,
        BigDecimal precoTotal,
        List<ItemEvento> itens,
        String correlationId,
        LocalDateTime criadoEm
) {
}
