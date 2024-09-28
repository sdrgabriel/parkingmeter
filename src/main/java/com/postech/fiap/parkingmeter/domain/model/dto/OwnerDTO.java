package com.postech.fiap.parkingmeter.domain.model.dto;

import com.postech.fiap.parkingmeter.domain.model.Owner;
import lombok.Builder;

@Builder
public record OwnerDTO(
        String id, String nome, String cpf, String telefone, String email, EnderecoDTO endereco) {

    public static OwnerDTO toDTO(Owner owner) {
        return OwnerDTO
                .builder()
                .id(owner.getId())
                .nome(owner.getNome())
                .cpf(owner.getCpf())
                .email(owner.getEmail())
                .telefone(owner.getTelefone())
                .endereco(
                        EnderecoDTO
                                .builder()
                                .logradouro(owner.getEndereco().getLogradouro())
                                .bairro(owner.getEndereco().getBairro())
                                .cidade(owner.getEndereco().getCidade())
                                .estado(owner.getEndereco().getEstado())
                                .complemento(owner.getEndereco().getComplemento())
                                .cep(owner.getEndereco().getCep())
                                .numero(owner.getEndereco().getNumero())
                                .build()
                )
                .build();
    }


}
