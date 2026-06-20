package br.com.farmaciadelivery.pedido_service.exception;

/** Falha de resiliência: o catalogo-service não respondeu (timeout/erro). Vira 503. */
public class CatalogoIndisponivelException extends RuntimeException {
    public CatalogoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
