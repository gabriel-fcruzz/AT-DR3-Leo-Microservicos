package br.com.farmaciadelivery.catalogo_service.controller;

import br.com.farmaciadelivery.catalogo_service.dto.DecrementarEstoqueDTO;
import br.com.farmaciadelivery.catalogo_service.dto.EstoqueDTO;
import br.com.farmaciadelivery.catalogo_service.dto.EstoqueRespostaDTO;
import br.com.farmaciadelivery.catalogo_service.model.Estabelecimento;
import br.com.farmaciadelivery.catalogo_service.model.Estoque;
import br.com.farmaciadelivery.catalogo_service.model.Produto;
import br.com.farmaciadelivery.catalogo_service.service.EstabelecimentoService;
import br.com.farmaciadelivery.catalogo_service.service.EstoqueService;
import br.com.farmaciadelivery.catalogo_service.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;
    private final EstabelecimentoService estabelecimentoService;
    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<EstoqueRespostaDTO> adicionar(@Valid @RequestBody EstoqueDTO dto) {
        Estabelecimento estabelecimento = estabelecimentoService.buscarPorId(dto.getEstabelecimentoId());
        Produto produto = produtoService.buscarPorId(dto.getProdutoId());

        Estoque estoque = new Estoque();
        estoque.setEstabelecimento(estabelecimento);
        estoque.setProduto(produto);
        estoque.setQuantidade(dto.getQuantidade());
        estoque.setPreco(dto.getPreco());

        Estoque salvo = estoqueService.adicionarAoEstoque(estoque);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EstoqueRespostaDTO.de(salvo));
    }

    @GetMapping("/estabelecimento/{id}")
    public ResponseEntity<List<EstoqueRespostaDTO>> listarPorEstabelecimento(@PathVariable Long id) {
        List<EstoqueRespostaDTO> resposta = estoqueService.listarPorEstabelecimento(id)
                .stream()
                .map(EstoqueRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/verificar")
    public ResponseEntity<Boolean> verificarDisponibilidade(
            @RequestParam Long estabelecimentoId,
            @RequestParam Long produtoId,
            @RequestParam Integer quantidade) {
        return ResponseEntity.ok(
                estoqueService.verificarDisponibilidade(estabelecimentoId, produtoId, quantidade));
    }

    @PostMapping("/decrementar")
    public ResponseEntity<Void> decrementar(@Valid @RequestBody DecrementarEstoqueDTO dto) {
        estoqueService.decrementarEstoque(dto.getEstabelecimentoId(), dto.getProdutoId(), dto.getQuantidade());
        return ResponseEntity.noContent().build();
    }
}