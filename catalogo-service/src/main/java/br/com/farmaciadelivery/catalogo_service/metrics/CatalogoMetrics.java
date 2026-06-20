package br.com.farmaciadelivery.catalogo_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Métrica de negócio: quantos eventos PedidoCriado foram consumidos do Kafka.
 * Exposta em /actuator/prometheus como eventos_pedido_consumidos_total.
 */
@Component
public class CatalogoMetrics {

    private final Counter eventosConsumidos;

    public CatalogoMetrics(MeterRegistry registry) {
        this.eventosConsumidos = Counter.builder("eventos_pedido_consumidos_total")
                .description("Total de eventos PedidoCriado consumidos do Kafka")
                .tag("service", "catalogo-service")
                .register(registry);
    }

    public void incrementarEventosConsumidos() {
        eventosConsumidos.increment();
    }
}
