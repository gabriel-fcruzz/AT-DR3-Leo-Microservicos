package br.com.farmaciadelivery.catalogo_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DecrementarEstoqueDTO {

    @NotNull
    private Long estabelecimentoId;

    @NotNull
    private Long produtoId;

    @NotNull
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    private Integer quantidade;
}