package br.com.farmaciadelivery.pedido_service.service;

import br.com.farmaciadelivery.pedido_service.domain.ItemPedido;
import br.com.farmaciadelivery.pedido_service.domain.Pedido;
import br.com.farmaciadelivery.pedido_service.domain.StatusPagamento;
import br.com.farmaciadelivery.pedido_service.domain.StatusPedido;
import br.com.farmaciadelivery.pedido_service.dto.ItemPedidoRequest;
import br.com.farmaciadelivery.pedido_service.dto.PedidoRequest;
import br.com.farmaciadelivery.pedido_service.exception.EstoqueInsuficienteException;
import br.com.farmaciadelivery.pedido_service.metrics.PedidoMetrics;
import br.com.farmaciadelivery.pedido_service.repository.PedidoRepository;
import br.com.farmaciadelivery.pedido_service.service.kafka.PedidoEventoProducer;
import br.com.farmaciadelivery.pedido_service.service.kafka.events.PedidoCriadoEvento;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CatalogoService catalogoService;
    private final PedidoEventoProducer eventoProducer;
    private final PedidoMetrics pedidoMetrics;

    @Transactional
    public Pedido criarPedido(PedidoRequest request) {
        // 1. Verificar disponibilidade de estoque ANTES de confirmar (síncrono, via catálogo).
        for (ItemPedidoRequest item : request.itens()) {
            boolean disponivel = catalogoService.verificarDisponibilidade(
                    request.estabelecimentoId(), item.produtoId(), item.quantidade());
            if (!disponivel) {
                throw new EstoqueInsuficienteException(
                        "Estoque insuficiente para o produto " + item.produtoId());
            }
        }

        // 2. Montar o pedido e calcular o preço total (Σ quantidade × precoUnitario).
        Pedido pedido = new Pedido();
        pedido.setEstabelecimentoId(request.estabelecimentoId());
        pedido.setStatus(StatusPedido.CONFIRMADO);
        pedido.setStatusPagamento(StatusPagamento.PAGAMENTO_SIMULADO_APROVADO);

        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedidoRequest item : request.itens()) {
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setProdutoId(item.produtoId());
            itemPedido.setQuantidade(item.quantidade());
            itemPedido.setPrecoUnitario(item.precoUnitario());
            pedido.adicionarItem(itemPedido);

            total = total.add(item.precoUnitario().multiply(BigDecimal.valueOf(item.quantidade())));
        }
        pedido.setPrecoTotal(total);

        // 3. Persistir (cascade salva os itens junto).
        Pedido salvo = pedidoRepository.save(pedido);

        // 4. Publicar o evento PedidoCriado (TP2): o decremento de estoque agora é ASSÍNCRONO via Kafka.
        //    O catalogo-service consome o evento e decrementa. O correlationId viaja no evento
        //    para correlacionar os logs entre os dois serviços.
        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        try {
            eventoProducer.publicar(PedidoCriadoEvento.fromPedido(salvo, correlationId));
            pedidoMetrics.incrementarPedidosCriados();
        } finally {
            MDC.remove("correlationId");
        }

        return salvo;
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + id));
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }
}
