package br.com.farmaciadelivery.pedido_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Métrica de negócio: quantos pedidos foram criados.
 * Exposta em /actuator/prometheus como pedidos_criados_total e coletada pelo Prometheus.
 */
@Component
public class PedidoMetrics {

    private final Counter pedidosCriados;

    public PedidoMetrics(MeterRegistry registry) {
        this.pedidosCriados = Counter.builder("pedidos_criados_total")
                .description("Total de pedidos criados")
                .tag("service", "pedido-service")
                .register(registry);
    }

    public void incrementarPedidosCriados() {
        pedidosCriados.increment();
    }
}
