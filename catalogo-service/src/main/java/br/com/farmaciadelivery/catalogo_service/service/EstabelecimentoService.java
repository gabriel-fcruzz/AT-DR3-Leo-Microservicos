package br.com.farmaciadelivery.catalogo_service.service;

import br.com.farmaciadelivery.catalogo_service.exception.RecursoNaoEncontradoException;
import br.com.farmaciadelivery.catalogo_service.model.Estabelecimento;
import br.com.farmaciadelivery.catalogo_service.repository.EstabelecimentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstabelecimentoService {

    private final EstabelecimentoRepository estabelecimentoRepository;

    public Estabelecimento criar(Estabelecimento estabelecimento) {
        log.info("Criando estabelecimento: {}", estabelecimento.getNome());
        return estabelecimentoRepository.save(estabelecimento);
    }

    public List<Estabelecimento> listarTodos() {
        return estabelecimentoRepository.findAll();
    }

    public Estabelecimento buscarPorId(Long id) {
        return estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Estabelecimento não encontrado com id: " + id));
    }
}