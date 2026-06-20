package br.com.farmaciadelivery.catalogo_service.dto;

import br.com.farmaciadelivery.catalogo_service.model.Estoque;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EstoqueRespostaDTO {

    private Long id;
    private Long estabelecimentoId;
    private String estabelecimentoNome;
    private Long produtoId;
    private String produtoNome;
    private Integer quantidade;
    private BigDecimal preco;
    private LocalDateTime atualizadoEm;

    public static EstoqueRespostaDTO de(Estoque estoque) {
        EstoqueRespostaDTO dto = new EstoqueRespostaDTO();
        dto.setId(estoque.getId());
        dto.setEstabelecimentoId(estoque.getEstabelecimento().getId());
        dto.setEstabelecimentoNome(estoque.getEstabelecimento().getNome());
        dto.setProdutoId(estoque.getProduto().getId());
        dto.setProdutoNome(estoque.getProduto().getNome());
        dto.setQuantidade(estoque.getQuantidade());
        dto.setPreco(estoque.getPreco());
        dto.setAtualizadoEm(estoque.getAtualizadoEm());
        return dto;
    }
}