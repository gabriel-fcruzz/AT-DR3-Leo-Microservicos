package br.com.farmaciadelivery.pedido_service.client;

import br.com.farmaciadelivery.pedido_service.dto.DecrementarEstoqueRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cliente declarativo para o catalogo-service.
 * O name "catalogo-service" é o nome lógico do serviço no Eureka (case-insensitive);
 * o Feign usa o load balancer para descobrir a instância, sem IP/porta fixos.
 * Em minúsculo para casar com a chave em spring.cloud.openfeign.client.config.catalogo-service.
 */
@FeignClient(name = "catalogo-service")
public interface CatalogoClient {

    @GetMapping("/estoque/verificar")
    Boolean verificarDisponibilidade(
            @RequestParam("estabelecimentoId") Long estabelecimentoId,
            @RequestParam("produtoId") Long produtoId,
            @RequestParam("quantidade") Integer quantidade);

    @PostMapping("/estoque/decrementar")
    void decrementarEstoque(@RequestBody DecrementarEstoqueRequest body);
}
