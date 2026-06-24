package br.com.farmaciadelivery.pedido_service.service;

import br.com.farmaciadelivery.pedido_service.client.CatalogoClient;
import br.com.farmaciadelivery.pedido_service.dto.DecrementarEstoqueRequest;
import br.com.farmaciadelivery.pedido_service.exception.CatalogoIndisponivelException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementação REAL: chama o catalogo-service via Feign.
 * Ativa só no profile "prod" (integração com Eureka + catálogo no ar).
 *
 * Resiliência (TP3): Resilience4j na verificação de estoque (GET idempotente):
 *   - @Retry: reenvia falha TRANSITÓRIA (conexão/timeout) — seguro porque é idempotente.
 *   - @CircuitBreaker: em falha SUSTENTADA abre e falha rápido, protegendo o catálogo.
 *   - Timeout continua sendo o do Feign (read/connect-timeout). Não usamos @TimeLimiter:
 *     ele só atua com retorno CompletableFuture/reativo; aqui a chamada Feign é síncrona.
 *
 * Aspect order do Resilience4j: Retry é o mais EXTERNO e envolve o CircuitBreaker. Por isso
 * o fallback fica no @Retry (dispara após esgotar as tentativas, ou quando o circuito está
 * aberto). Há um fallback POR TIPO de exceção; o que NÃO casar com nenhum sobe (não engolimos bug).
 */
@Service
@Profile("prod")
@RequiredArgsConstructor
public class CatalogoServiceImpl implements CatalogoService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoServiceImpl.class);

    private final CatalogoClient catalogoClient;

    @CircuitBreaker(name = "catalogo")
    @Retry(name = "catalogo", fallbackMethod = "fallbackVerificar")
    @Override
    public boolean verificarDisponibilidade(Long estabelecimentoId, Long produtoId, Integer quantidade) {
        Boolean disponivel = catalogoClient.verificarDisponibilidade(estabelecimentoId, produtoId, quantidade);
        return Boolean.TRUE.equals(disponivel);
    }

    /**
     * Fallback de falha SUSTENTADA: circuito aberto. Falha rápido sem nem tocar no catálogo.
     * Resilience4j escolhe este por casar o tipo CallNotPermittedException.
     */
    @SuppressWarnings("unused")
    private boolean fallbackVerificar(Long estabelecimentoId, Long produtoId, Integer quantidade,
                                      CallNotPermittedException ex) {
        log.warn("Circuito 'catalogo' ABERTO — falha rápida sem chamar o catálogo (produtoId={})", produtoId);
        throw new CatalogoIndisponivelException(
                "Catálogo instável (circuito aberto). Tente novamente em instantes.");
    }

    /**
     * Fallback de falha TRANSITÓRIA que esgotou as tentativas: conexão/timeout do Feign.
     * Fail-safe: não confirmar pedido sem validar estoque -> 503.
     */
    @SuppressWarnings("unused")
    private boolean fallbackVerificar(Long estabelecimentoId, Long produtoId, Integer quantidade,
                                      FeignException ex) {
        log.warn("Catálogo indisponível ao verificar estoque (produtoId={}): {}", produtoId, ex.getMessage());
        throw new CatalogoIndisponivelException("Serviço de catálogo temporariamente indisponível");
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
