package br.com.farmaciadelivery.catalogo_service.controller;

import br.com.farmaciadelivery.catalogo_service.dto.EstabelecimentoDTO;
import br.com.farmaciadelivery.catalogo_service.dto.EstabelecimentoRespostaDTO;
import br.com.farmaciadelivery.catalogo_service.model.Estabelecimento;
import br.com.farmaciadelivery.catalogo_service.service.EstabelecimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estabelecimentos")
@RequiredArgsConstructor
public class EstabelecimentoController {

    private final EstabelecimentoService estabelecimentoService;

    @PostMapping
    public ResponseEntity<EstabelecimentoRespostaDTO> criar(@Valid @RequestBody EstabelecimentoDTO dto) {
        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setNome(dto.getNome());
        estabelecimento.setEndereco(dto.getEndereco());
        estabelecimento.setTelefone(dto.getTelefone());
        estabelecimento.setHorarioFuncionamento(dto.getHorarioFuncionamento());

        Estabelecimento salvo = estabelecimentoService.criar(estabelecimento);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EstabelecimentoRespostaDTO.de(salvo));
    }

    @GetMapping
    public ResponseEntity<List<EstabelecimentoRespostaDTO>> listar() {
        List<EstabelecimentoRespostaDTO> resposta = estabelecimentoService.listarTodos()
                .stream()
                .map(EstabelecimentoRespostaDTO::de)
                .toList();
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstabelecimentoRespostaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(EstabelecimentoRespostaDTO.de(estabelecimentoService.buscarPorId(id)));
    }
}