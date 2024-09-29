package com.postech.fiap.parkingmeter.domain.model;

import com.postech.fiap.parkingmeter.domain.model.dto.OwnerDTO;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
public class Owner {

    @Id
    private String id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private Endereco endereco;

    public static Owner toEntity(OwnerDTO ownerDTO) {
        return Owner
                .builder()
                .id(ownerDTO.id())
                .nome(ownerDTO.nome())
                .cpf(ownerDTO.cpf())
                .email(ownerDTO.email())
                .telefone(ownerDTO.telefone())
                .endereco(
                        Endereco
                                .builder()
                                .logradouro(ownerDTO.endereco().logradouro())
                                .bairro(ownerDTO.endereco().bairro())
                                .cidade(ownerDTO.endereco().cidade())
                                .estado(ownerDTO.endereco().estado())
                                .complemento(ownerDTO.endereco().complemento())
                                .cep(ownerDTO.endereco().cep())
                                .numero(ownerDTO.endereco().numero())
                                .build()
                )
                .build();
    }

}
