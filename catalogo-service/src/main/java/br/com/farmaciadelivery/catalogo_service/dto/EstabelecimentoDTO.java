package br.com.farmaciadelivery.catalogo_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EstabelecimentoDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    private String endereco;
    private String telefone;
    private String horarioFuncionamento;
}