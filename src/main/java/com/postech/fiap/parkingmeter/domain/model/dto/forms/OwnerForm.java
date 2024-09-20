package com.postech.fiap.parkingmeter.domain.model.dto.forms;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OwnerForm {

  @NotNull
  @NotEmpty
  @Size(max = 78)
  private String examples;
}
