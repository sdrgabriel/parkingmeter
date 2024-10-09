package com.postech.fiap.parkingmeter.domain.model;

import com.postech.fiap.parkingmeter.domain.model.enums.PaymentStatusEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ticket")
@Data
public class Ticket {
  @Id private String id;

  @Valid
  @NotNull
  @Field("total_amount_charged")
  private double totalAmountCharged;

  @Valid
  @NotNull
  @Indexed
  @Field("start_time")
  private LocalDateTime startTime;

  @Valid
  @Indexed
  @Field("end_time")
  private LocalDateTime endTime;

  @Valid
  @NotNull
  @Indexed
  @Field("payment_status")
  private PaymentStatusEnum paymentStatus;

  @Valid @NotNull @Indexed private ParkingMeter parkingMeter;

  @Valid @NotNull @Indexed private Vehicle vehicle;
}
