package br.com.farmaciadelivery.pedido_service.exception;

/** Regra de negócio: não há estoque suficiente para confirmar o pedido. */
public class EstoqueInsuficienteException extends RuntimeException {
    public EstoqueInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
