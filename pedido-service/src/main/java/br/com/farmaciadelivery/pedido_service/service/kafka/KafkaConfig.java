package br.com.farmaciadelivery.pedido_service.service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Cria o tópico no broker ao subir (só no profile prod, onde o Kafka é real).
 * 1 partição e 1 réplica — suficiente para o ambiente de aula.
 */
@Configuration
@Profile("prod")
public class KafkaConfig {

    public static final String TOPICO_PEDIDOS_CRIADOS = "pedidos.criados";

    @Bean
    public NewTopic pedidosCriadosTopic() {
        return new NewTopic(TOPICO_PEDIDOS_CRIADOS, 1, (short) 1);
    }
}
