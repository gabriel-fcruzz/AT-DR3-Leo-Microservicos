package br.com.farmaciadelivery.pedido_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Implementação MOCK: não chama o catálogo de verdade.
 * Ativa em dev/default — permite rodar o pedido-service isolado, sem Eureka nem catálogo no ar.
 */
@Service
@Profile({"dev", "default"})
public class CatalogoServiceMock implements CatalogoService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoServiceMock.class);

    @Override
    public boolean verificarDisponibilidade(Long estabelecimentoId, Long produtoId, Integer quantidade) {
        log.info("[MOCK] Verificando disponibilidade (produtoId={}) -> sempre true", produtoId);
        return true;
    }

    @Override
    public void decrementarEstoque(Long estabelecimentoId, Long produtoId, Integer quantidade) {
        log.info("[MOCK] Decrementando estoque (produtoId={}, quantidade={}) -> no-op", produtoId, quantidade);
    }
}
