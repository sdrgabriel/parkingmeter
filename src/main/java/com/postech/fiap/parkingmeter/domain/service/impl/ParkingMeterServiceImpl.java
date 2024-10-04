package com.postech.fiap.parkingmeter.domain.service.impl;

import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;
import com.postech.fiap.parkingmeter.domain.model.dto.*;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.ParkingMeterForm;
import com.postech.fiap.parkingmeter.domain.model.enums.StatusPagamentoEnum;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Endereco;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.HorarioFuncionamento;
import com.postech.fiap.parkingmeter.domain.repository.ParkingMeterRepository;
import com.postech.fiap.parkingmeter.domain.service.ParkingMeterService;
import com.postech.fiap.parkingmeter.domain.util.ConverterToDTO;
import com.postech.fiap.parkingmeter.infrastructure.exception.ParkingMeterException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@ImportAutoConfiguration(TransactionAutoConfiguration.class)
@Transactional
@RequiredArgsConstructor
public class ParkingMeterServiceImpl implements ParkingMeterService {

  private final ParkingMeterRepository parkingMeterRepository;
  private final ConverterToDTO converterToDTO;
  private final MongoTemplate mongoTemplate;

  @Override
  @Transactional(readOnly = true)
  public Page<ParkingMeterDTO> findAll(Pageable pageable) {
    log.info("Find all Parking Meters");
    return this.parkingMeterRepository.findAll(pageable).map(this.converterToDTO::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public ParkingMeterDTO getById(String id) {
    log.info("Find one Parking Meter");
    var parkingMeter = this.getParkingMeter(id);
    return converterToDTO.toDto(parkingMeter);
  }

  @Override
  public ParkingMeterDTO create(ParkingMeterForm parkingMeterForm) {
    try {
      log.info("Create Parking Meter");

      boolean cepExists = this.parkingMeterRepository.existsByEndereco_Cep(parkingMeterForm.cep());

      if (cepExists) {
        throw new ParkingMeterException(
            "Parking meter with CEP " + parkingMeterForm.cep() + " already exists.",
            HttpStatus.CONFLICT);
      }

      var parkingMeter = preencherParkingMeter(null, parkingMeterForm);
      return converterToDTO.toDto(this.parkingMeterRepository.save(parkingMeter));
    } catch (Exception e) {
      throw new ParkingMeterException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public ParkingMeterDTO updateById(String id, ParkingMeterForm parkingMeterForm) {
    log.info("Update Parking Meter");
    this.getParkingMeter(id);
    var update = preencherParkingMeter(id, parkingMeterForm);
    return this.converterToDTO.toDto(this.parkingMeterRepository.save(update));
  }

  @Override
  public void deleteById(String id) {
    log.info("Delete Parking Meter");
    this.parkingMeterRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Endereco getEnderecoByCep(String cep) {
    log.info("Get Endereço by cep");
    try {
      RestTemplate restTemplate = new RestTemplate();
      String url = "http://viacep.com.br/ws/%s/json".formatted(cep.replace("-", ""));
      return restTemplate.getForObject(url, Endereco.class);
    } catch (Exception e) {
      throw new ParkingMeterException(
          "Error retrieving address, please evaluate zip code: %s".formatted(cep),
          HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  @Cacheable(value = "rankedParkingMetersByDate", key = "#startDate + '_' + #endDate")
  @Transactional(readOnly = true)
  public Slice<ParkingMeterArrecadacaoDTO> getParquimetroMaisArrecadado(
      String dataInicio, String dataFim, Pageable pageable) {

    Instant startDate = Instant.parse(dataInicio);
    Instant endDate = Instant.parse(dataFim);

    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(Criteria.where("horario_inicio").gte(startDate).lte(endDate)),
            Aggregation.group("parquimetro._id")
                .first("parquimetro")
                .as("parquimetro")
                .sum("valor_total_cobrado")
                .as("totalArrecadado"),
            Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalArrecadado")),
            Aggregation.skip((long) pageable.getOffset()),
            Aggregation.limit(pageable.getPageSize()));

    AggregationResults<ParkingMeterArrecadacaoDTO> results =
        mongoTemplate.aggregate(aggregation, "ticket", ParkingMeterArrecadacaoDTO.class);

    List<ParkingMeterArrecadacaoDTO> resultList = results.getMappedResults();
    boolean hasNext = resultList.size() == pageable.getPageSize();

    return new SliceImpl<>(resultList, pageable, hasNext);
  }

  @Override
  @Transactional(readOnly = true)
  public ParkingSpaceDTO getAvailableSpace(String id, LocalDate date) {
    log.info("get Available Spaces");

    var parkingMeter = getParkingMeter(id);

    MatchOperation matchParkingMeter = Aggregation.match(Criteria.where("_id").is(id));

    LookupOperation lookupOperation =
        Aggregation.lookup("ticket", "_id", "parquimetro._id", "tickets");

    UnwindOperation unwindTickets = Aggregation.unwind("tickets", "index", Boolean.TRUE);

    MatchOperation matchPendingTickets =
        Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where("index").is(null),
                    new Criteria()
                        .andOperator(
                            Criteria.where("tickets.status_pagamento")
                                .is(StatusPagamentoEnum.PENDENTE),
                            Criteria.where("tickets.horario_inicio")
                                .gte(date.atStartOfDay())
                                .lte(date.plusDays(1).atStartOfDay()))));

    GroupOperation groupTickets =
        Aggregation.group("tickets.parquimetro._id")
            .first("vagas_disponiveis")
            .as("spaces")
            .first("endereco")
            .as("endereco")
            .first("index")
            .as("idx")
            .count()
            .as("ocupadas");

    ProjectionOperation projectFields =
        Aggregation.project("_id", "endereco", "spaces", "idx")
            .and(AggregationSpELExpression.expressionOf("cond(idx==null, spaces, spaces-ocupadas)"))
            .as("available");

    TypedAggregation<ParkingMeter> aggregation =
        Aggregation.newAggregation(
            ParkingMeter.class,
            matchParkingMeter,
            lookupOperation,
            unwindTickets,
            matchPendingTickets,
            groupTickets,
            projectFields);

    AggregationResults<ParkingSpaceDTO> results =
        mongoTemplate.aggregate(aggregation, "parkingmeter", ParkingSpaceDTO.class);
    var listResult = results.getMappedResults();

    ParkingSpaceDTO parkingSpaceDTO = new ParkingSpaceDTO();
    if (listResult.isEmpty()) {
      parkingSpaceDTO.setDate(LocalDateTime.now());
      parkingSpaceDTO.setEndereco(parkingMeter.getEndereco());
      parkingSpaceDTO.setSpaces(parkingMeter.getVagasDisponiveis());
      parkingSpaceDTO.setAvailable(0);
    } else {
      parkingSpaceDTO = listResult.get(0);
      parkingSpaceDTO.setDate(LocalDateTime.now());
    }

    return parkingSpaceDTO;
  }

  @Override
  @Transactional(readOnly = true)
  public TimesParkedDTO getTimesParked(String parkingMeterId, String licensePlate) {
    getParkingMeter(parkingMeterId);

    MatchOperation matchParkingMeter = Aggregation.match(Criteria.where("_id").is(parkingMeterId));

    LookupOperation lookupOperation =
        Aggregation.lookup("ticket", "_id", "parquimetro._id", "tickets");

    UnwindOperation unwindTickets = Aggregation.unwind("tickets", "index", Boolean.TRUE);

    MatchOperation matchPendingTickets =
        Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where("index").is(null),
                    Criteria.where("tickets.veiculo.license_plate").is(licensePlate)));

    GroupOperation groupTickets =
        Aggregation.group("tickets.veiculo._id").first("index").as("idx").count().as("cont_parked");

    ProjectionOperation projectFields =
        Aggregation.project("_id", "idx", "cont_parked")
            .and(AggregationSpELExpression.expressionOf("cond(idx==null, 0, cont_parked)"))
            .as("timesParked");

    TypedAggregation<ParkingMeter> aggregation =
        Aggregation.newAggregation(
            ParkingMeter.class,
            matchParkingMeter,
            lookupOperation,
            unwindTickets,
            matchPendingTickets,
            groupTickets,
            projectFields);

    AggregationResults<TimesParkedDTO> results =
        mongoTemplate.aggregate(aggregation, "parkingmeter", TimesParkedDTO.class);
    var listResult = results.getMappedResults();

    TimesParkedDTO timesParkedDTO = new TimesParkedDTO();
    if (listResult.isEmpty()) {
      timesParkedDTO.setDate(LocalDateTime.now());
      timesParkedDTO.setTimesParked(0);
    } else {
      timesParkedDTO = listResult.get(0);
      timesParkedDTO.setDate(LocalDateTime.now());
    }
    return timesParkedDTO;
  }

  @Override
  @Transactional(readOnly = true)
  public TimesParkedDTO getTimesParkedWithDateRange(
      String parkingMeterId, String licensePlate, LocalDate begin, LocalDate end) {
    getParkingMeter(parkingMeterId);

    if (end == null) {
      end = LocalDate.now();
    }

    if (begin.isAfter(end)) {
      throw new ParkingMeterException(
          "Data begin is greater than data end", HttpStatus.BAD_REQUEST);
    }

    MatchOperation matchParkingMeter = Aggregation.match(Criteria.where("_id").is(parkingMeterId));

    LookupOperation lookupOperation =
        Aggregation.lookup("ticket", "_id", "parquimetro._id", "tickets");

    UnwindOperation unwindTickets = Aggregation.unwind("tickets", "index", Boolean.TRUE);

    MatchOperation matchPendingTickets =
        Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where("index").is(null),
                    new Criteria()
                        .andOperator(
                            Criteria.where("tickets.veiculo.license_plate").is(licensePlate),
                            Criteria.where("tickets.horario_inicio")
                                .gte(begin.atStartOfDay())
                                .lt(end.plusDays(1)))));

    GroupOperation groupTickets =
        Aggregation.group("tickets.veiculo._id").first("index").as("idx").count().as("cont_parked");

    ProjectionOperation projectFields =
        Aggregation.project("_id", "idx", "cont_parked")
            .and(AggregationSpELExpression.expressionOf("cond(idx==null, 0, cont_parked)"))
            .as("timesParked");

    TypedAggregation<ParkingMeter> aggregation =
        Aggregation.newAggregation(
            ParkingMeter.class,
            matchParkingMeter,
            lookupOperation,
            unwindTickets,
            matchPendingTickets,
            groupTickets,
            projectFields);

    AggregationResults<TimesParkedDTO> results =
        mongoTemplate.aggregate(aggregation, "parkingmeter", TimesParkedDTO.class);
    var listResult = results.getMappedResults();

    TimesParkedDTO timesParkedDTO = new TimesParkedDTO();
    if (listResult.isEmpty()) {
      timesParkedDTO.setDate(LocalDateTime.now());
      timesParkedDTO.setTimesParked(0);
    } else {
      timesParkedDTO = listResult.get(0);
      timesParkedDTO.setDate(LocalDateTime.now());
    }
    return timesParkedDTO;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ParkingMeterDTO> findAllByCidadeOrBairro(
      String cidade, String bairro, Pageable pageable) {
    if (cidade == null && bairro == null) {
      throw new ParkingMeterException(
          "At least one filter parameter must be entered", HttpStatus.BAD_REQUEST);
    }
    return this.parkingMeterRepository
        .findAllByCidadeOrBairro(cidade, bairro, pageable)
        .map(converterToDTO::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public AmountEarnedDTO getParkingMeterEarnedWithDataRange(
      String parkingMeterId, LocalDate begin, LocalDate end) {
    var parkingMeter = getParkingMeter(parkingMeterId);

    if (end == null) {
      end = LocalDate.now();
    }

    if (begin.isAfter(end)) {
      throw new ParkingMeterException(
          "Data begin is greater than data end", HttpStatus.BAD_REQUEST);
    }

    MatchOperation matchParkingMeter = Aggregation.match(Criteria.where("_id").is(parkingMeterId));

    LookupOperation lookupOperation =
        Aggregation.lookup("ticket", "_id", "parquimetro._id", "tickets");

    UnwindOperation unwindTickets = Aggregation.unwind("tickets", "index", Boolean.TRUE);

    MatchOperation matchPendingTickets =
        Aggregation.match(
            new Criteria()
                .andOperator(
                    Criteria.where("tickets.status_pagamento").is(StatusPagamentoEnum.PAGO),
                    Criteria.where("tickets.horario_inicio")
                        .gte(begin.atStartOfDay())
                        .lt(end.plusDays(1))));

    GroupOperation groupTickets =
        Aggregation.group("_id").sum("tickets.valor_total_cobrado").as("earned");

    ProjectionOperation projectFields = Aggregation.project("_id", "earned");

    TypedAggregation<ParkingMeter> aggregation =
        Aggregation.newAggregation(
            ParkingMeter.class,
            matchParkingMeter,
            lookupOperation,
            unwindTickets,
            matchPendingTickets,
            groupTickets,
            projectFields);

    AggregationResults<AmountEarnedDTO> results =
        mongoTemplate.aggregate(aggregation, "parkingmeter", AmountEarnedDTO.class);
    var listResult = results.getMappedResults();

    AmountEarnedDTO amountEarnedDTO =
        AmountEarnedDTO.builder()
            .id(parkingMeter.getId())
            .endereco(parkingMeter.getEndereco())
            .date(LocalDateTime.now())
            .earned(0.0)
            .build();
    if (!listResult.isEmpty()) {
      amountEarnedDTO.setEarned(listResult.get(0).getEarned());
    }
    return amountEarnedDTO;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AmountEarnedByLocalityDTO> getParkingMeterEarnedWithDataRangeByLocality(
      String cidade, String bairro, LocalDate begin, LocalDate end, Pageable pageable) {
    if (ObjectUtils.isEmpty(cidade) && ObjectUtils.isEmpty(bairro)) {
      throw new ParkingMeterException(
          "At least one filter parameter must be entered", HttpStatus.BAD_REQUEST);
    }

    if (end == null) {
      end = LocalDate.now();
    }

    if (begin.isAfter(end)) {
      throw new ParkingMeterException(
          "Data begin is greater than data end", HttpStatus.BAD_REQUEST);
    }

    List<Criteria> criteriaList = new ArrayList<>();
    if (!ObjectUtils.isEmpty(cidade)) {
      criteriaList.add(Criteria.where("endereco.cidade").is(cidade));
    }

    if (!ObjectUtils.isEmpty(bairro)) {
      criteriaList.add(Criteria.where("endereco.bairro").is(bairro));
    }

    MatchOperation matchParkingMeter =
        Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

    LookupOperation lookupOperation =
        Aggregation.lookup("ticket", "_id", "parquimetro._id", "tickets");

    UnwindOperation unwindTickets = Aggregation.unwind("tickets", "index", Boolean.TRUE);

    MatchOperation matchPendingTickets =
        Aggregation.match(
            new Criteria()
                .andOperator(
                    Criteria.where("tickets.status_pagamento").is(StatusPagamentoEnum.PAGO),
                    Criteria.where("tickets.horario_inicio")
                        .gte(begin.atStartOfDay())
                        .lt(end.plusDays(1))));

    GroupOperation groupTickets =
        Aggregation.group("_id")
            .first("endereco")
            .as("endereco")
            .sum("tickets.valor_total_cobrado")
            .as("earned");

    ProjectionOperation projectFields =
        Aggregation.project("endereco", "earned").and("_id").as("id");

    TypedAggregation<ParkingMeter> aggregation =
        Aggregation.newAggregation(
            ParkingMeter.class,
            matchParkingMeter,
            lookupOperation,
            unwindTickets,
            matchPendingTickets,
            groupTickets,
            projectFields);

    AggregationResults<AmountEarnedByLocalityDTO> results =
        mongoTemplate.aggregate(aggregation, "parkingmeter", AmountEarnedByLocalityDTO.class);

    var listResult = results.getMappedResults();

    Page<AmountEarnedByLocalityDTO> pageAmountEarnedByLocalityDTO = null;

    if (!listResult.isEmpty()) {
      int startPage = (int) pageable.getOffset();
      int endPage = Math.min((startPage + pageable.getPageSize()), listResult.size());

      List<AmountEarnedByLocalityDTO> pageContent = listResult.subList(startPage, endPage);
      pageAmountEarnedByLocalityDTO = new PageImpl<>(pageContent, pageable, listResult.size());
    } else {
      pageAmountEarnedByLocalityDTO = new PageImpl<>(new ArrayList<>(), Pageable.unpaged(), 1);
    }

    return pageAmountEarnedByLocalityDTO;
  }

  private ParkingMeter preencherParkingMeter(String id, ParkingMeterForm parkingMeterForm) {
    log.info("Building object Parking Meter");
    final Endereco endereco = getEnderecoByCep(parkingMeterForm.cep());
    validaHorarioFuncionamento(parkingMeterForm.horarioFuncionamento());
    return new ParkingMeter(
        id,
        parkingMeterForm.horarioFuncionamento(),
        parkingMeterForm.tarifa(),
        parkingMeterForm.vagasDisponiveis(),
        endereco,
        id != null ? this.getParkingMeter(id).getVersion() : null);
  }

  private void validaHorarioFuncionamento(HorarioFuncionamento hr) {
    log.info("Validating Horario Funcionamento");
    LocalTime inicio;
    LocalTime fim;
    try {
      String[] slicedHour = hr.inicio().split(":");
      inicio = LocalTime.of(Integer.parseInt(slicedHour[0]), Integer.parseInt(slicedHour[1]));
      slicedHour = hr.fim().split(":");
      fim = LocalTime.of(Integer.parseInt(slicedHour[0]), Integer.parseInt(slicedHour[1]));
    } catch (Exception e) {
      throw new ParkingMeterException(
          "problem with the HorarioFuncionamento check if it was filled correctly HH:mm",
          HttpStatus.BAD_REQUEST);
    }

    if (inicio.isAfter(fim)) {
      throw new ParkingMeterException("Start time greater than end time", HttpStatus.BAD_REQUEST);
    }
  }

  private ParkingMeter getParkingMeter(String id) {
    return this.parkingMeterRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ParkingMeterException(
                    "Parking Meter code does not exist", HttpStatus.NOT_FOUND));
  }
}
