package br.com.farmaciadelivery.pedido_service.dto;

/** Resposta padronizada de erro (mesmo formato do projeto do professor). */
public record ErrorResponse(
        String codigo,
        String mensagem
) {
}
