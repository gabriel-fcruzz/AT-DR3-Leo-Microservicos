package br.com.farmaciadelivery.pedido_service.service;

import br.com.farmaciadelivery.pedido_service.client.CatalogoClient;
import br.com.farmaciadelivery.pedido_service.dto.DecrementarEstoqueRequest;
import br.com.farmaciadelivery.pedido_service.exception.CatalogoIndisponivelException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementação REAL: chama o catalogo-service via Feign.
 * Ativa só no profile "prod" (integração com Eureka + catálogo no ar).
 *
 * Resiliência (TP1 item 9): Timeout (configurado no Feign) + Fallback (try/catch aqui).
 * Sem Resilience4J — Feign puro, como o professor ensinou.
 */
@Service
@Profile("prod")
@RequiredArgsConstructor
public class CatalogoServiceImpl implements CatalogoService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoServiceImpl.class);

    private final CatalogoClient catalogoClient;

    @Override
    public boolean verificarDisponibilidade(Long estabelecimentoId, Long produtoId, Integer quantidade) {
        try {
            Boolean disponivel = catalogoClient.verificarDisponibilidade(estabelecimentoId, produtoId, quantidade);
            return Boolean.TRUE.equals(disponivel);
        } catch (Exception e) {
            // Fallback: o catálogo caiu ou estourou o timeout. Fail-safe: não confirmar pedido sem validar estoque.
            log.warn("Catálogo indisponível ao verificar estoque (produtoId={}): {}", produtoId, e.getMessage());
            throw new CatalogoIndisponivelException("Serviço de catálogo temporariamente indisponível");
        }
    }

    @Override
    public void decrementarEstoque(Long estabelecimentoId, Long produtoId, Integer quantidade) {
        try {
            catalogoClient.decrementarEstoque(
                    new DecrementarEstoqueRequest(estabelecimentoId, produtoId, quantidade));
        } catch (Exception e) {
            // Fallback: o pedido JÁ foi confirmado. Não desfazemos; apenas registramos a falha.
            // Esta é exatamente a inconsistência que o Kafka resolve no TP2 (decremento assíncrono).
            log.error("Falha ao decrementar estoque no catálogo (produtoId={}): {}. "
                    + "Pedido permanece confirmado; reconciliação será tratada via Kafka no TP2.",
                    produtoId, e.getMessage());
        }
    }
}
