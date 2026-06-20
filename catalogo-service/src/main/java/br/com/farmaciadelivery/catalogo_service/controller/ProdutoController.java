package br.com.farmaciadelivery.catalogo_service.controller;

import br.com.farmaciadelivery.catalogo_service.dto.ProdutoDTO;
import br.com.farmaciadelivery.catalogo_service.dto.ProdutoRespostaDTO;
import br.com.farmaciadelivery.catalogo_service.model.Produto;
import br.com.farmaciadelivery.catalogo_service.model.ProdutoDocumento;
import br.com.farmaciadelivery.catalogo_service.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<ProdutoRespostaDTO> criar(@Valid @RequestBody ProdutoDTO dto) {
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setCategoria(dto.getCategoria());
        produto.setFabricante(dto.getFabricante());

        Produto salvo = produtoService.criar(produto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProdutoRespostaDTO.de(salvo));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoRespostaDTO>> listar() {
        List<ProdutoRespostaDTO> resposta = produtoService.listarTodos()
                .stream()
                .map(ProdutoRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoRespostaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ProdutoRespostaDTO.de(produtoService.buscarPorId(id)));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProdutoDocumento>> buscar(@RequestParam String termo) {
        return ResponseEntity.ok(produtoService.buscar(termo));
    }
}