package br.com.farmaciadelivery.pedido_service.controller;

import br.com.farmaciadelivery.pedido_service.dto.ErrorResponse;
import br.com.farmaciadelivery.pedido_service.exception.CatalogoIndisponivelException;
import br.com.farmaciadelivery.pedido_service.exception.EstoqueInsuficienteException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Pedido não encontrado -> 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePedidoNaoEncontrado(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("PEDIDO_NAO_ENCONTRADO", ex.getMessage()));
    }

    // Regra de negócio (sem estoque) -> 422
    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleEstoqueInsuficiente(EstoqueInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("ESTOQUE_INSUFICIENTE", ex.getMessage()));
    }

    // Validação do corpo (@Valid) -> 422
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Requisição inválida");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("REQUISICAO_INVALIDA", mensagem));
    }

    // Resiliência: catálogo fora do ar -> 503
    @ExceptionHandler(CatalogoIndisponivelException.class)
    public ResponseEntity<ErrorResponse> handleCatalogoIndisponivel(CatalogoIndisponivelException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("CATALOGO_INDISPONIVEL", ex.getMessage()));
    }
}
