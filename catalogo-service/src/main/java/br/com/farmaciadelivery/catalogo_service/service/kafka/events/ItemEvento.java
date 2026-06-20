package br.com.farmaciadelivery.catalogo_service.service.kafka.events;

/** Cópia local do item do evento (mesmos campos do produtor). */
public record ItemEvento(
        Long produtoId,
        Integer quantidade
) {
}
