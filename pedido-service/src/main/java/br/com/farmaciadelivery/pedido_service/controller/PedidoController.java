package br.com.farmaciadelivery.pedido_service.controller;

import br.com.farmaciadelivery.pedido_service.domain.Pedido;
import br.com.farmaciadelivery.pedido_service.dto.PedidoRequest;
import br.com.farmaciadelivery.pedido_service.dto.PedidoResponse;
import br.com.farmaciadelivery.pedido_service.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        Pedido pedido = pedidoService.criarPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PedidoResponse.fromDomain(pedido));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(PedidoResponse.fromDomain(pedidoService.buscarPorId(id)));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listar() {
        List<PedidoResponse> resposta = pedidoService.listarTodos()
                .stream()
                .map(PedidoResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(resposta);
    }
}
