package br.com.farmaciadelivery.catalogo_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProdutoDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    private String descricao;
    private String categoria;
    private String fabricante;
}