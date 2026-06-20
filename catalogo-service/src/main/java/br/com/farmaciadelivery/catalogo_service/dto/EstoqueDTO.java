package br.com.farmaciadelivery.catalogo_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EstoqueDTO {

    @NotNull(message = "ID do estabelecimento é obrigatório")
    private Long estabelecimentoId;

    @NotNull(message = "ID do produto é obrigatório")
    private Long produtoId;

    @NotNull
    @Min(value = 0, message = "Quantidade não pode ser negativa")
    private Integer quantidade;

    @NotNull
    private BigDecimal preco;
}