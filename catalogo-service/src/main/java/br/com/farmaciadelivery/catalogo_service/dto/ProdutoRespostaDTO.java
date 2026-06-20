package br.com.farmaciadelivery.catalogo_service.dto;

import br.com.farmaciadelivery.catalogo_service.model.Produto;
import lombok.Data;

@Data
public class ProdutoRespostaDTO {

    private Long id;
    private String nome;
    private String descricao;
    private String categoria;
    private String fabricante;

    public static ProdutoRespostaDTO de(Produto p) {
        ProdutoRespostaDTO dto = new ProdutoRespostaDTO();
        dto.setId(p.getId());
        dto.setNome(p.getNome());
        dto.setDescricao(p.getDescricao());
        dto.setCategoria(p.getCategoria());
        dto.setFabricante(p.getFabricante());
        return dto;
    }
}