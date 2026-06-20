package br.com.farmaciadelivery.catalogo_service.dto;

import br.com.farmaciadelivery.catalogo_service.model.Estabelecimento;
import lombok.Data;

@Data
public class EstabelecimentoRespostaDTO {

    private Long id;
    private String nome;
    private String endereco;
    private String telefone;
    private String horarioFuncionamento;

    public static EstabelecimentoRespostaDTO de(Estabelecimento e) {
        EstabelecimentoRespostaDTO dto = new EstabelecimentoRespostaDTO();
        dto.setId(e.getId());
        dto.setNome(e.getNome());
        dto.setEndereco(e.getEndereco());
        dto.setTelefone(e.getTelefone());
        dto.setHorarioFuncionamento(e.getHorarioFuncionamento());
        return dto;
    }
}