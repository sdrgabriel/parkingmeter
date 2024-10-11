package com.postech.fiap.parkingmeter.domain.service.impl;

import com.postech.fiap.parkingmeter.domain.model.Address;
import com.postech.fiap.parkingmeter.domain.model.OperationHours;
import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;
import com.postech.fiap.parkingmeter.domain.model.Rate;
import com.postech.fiap.parkingmeter.domain.model.dto.*;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.parkingmeter.OperatingHoursParkingForm;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.parkingmeter.ParkingMeterForm;
import com.postech.fiap.parkingmeter.domain.model.enums.PaymentStatusEnum;
import com.postech.fiap.parkingmeter.domain.repository.ParkingMeterRepository;
import com.postech.fiap.parkingmeter.domain.service.ParkingMeterService;
import com.postech.fiap.parkingmeter.domain.util.ConverterToDTO;
import com.postech.fiap.parkingmeter.infrastructure.exception.ParkingMeterException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
@Slf4j
@ImportAutoConfiguration(TransactionAutoConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Transactional
public class ParkingMeterServiceImpl implements ParkingMeterService {

  private final ParkingMeterRepository parkingMeterRepository;
  private final ConverterToDTO converterToDTO;
  private final MongoTemplate mongoTemplate;
  private static final String PARKING_METER_ID = "parkingMeter._id";
  private static final String TICKETS = "tickets";
  private static final String INDEX = "index";

  @Override
  @Transactional(readOnly = true)
  public Page<ParkingMeterDTO> findAll(Pageable pageable) {
    log.info("Find all Parking Meters");
    return parkingMeterRepository.findAll(pageable).map(converterToDTO::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public ParkingMeterDTO getById(String id) {
    log.info("Find one Parking Meter");
    return converterToDTO.toDto(getParkingMeter(id));
  }

  @Override
  public ParkingMeterDTO create(ParkingMeterForm parkingMeterForm) {
    try {
      log.info("Create Parking Meter");

      boolean zipCodeExists =
          parkingMeterRepository.existsByAddress_ZipCode(parkingMeterForm.address().zipCode());

      if (zipCodeExists) {
        throw new ParkingMeterException(
            "Parking meter with ZIP code "
                + parkingMeterForm.address().zipCode()
                + " already exists.",
            HttpStatus.CONFLICT);
      }

      var parkingMeter = populateParkingMeter(null, parkingMeterForm);
      return converterToDTO.toDto(parkingMeterRepository.save(parkingMeter));
    } catch (Exception e) {
      throw new ParkingMeterException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public ParkingMeterDTO updateById(String id, ParkingMeterForm parkingMeterForm) {
    log.info("Update Parking Meter");
    var parkingMeter = this.getParkingMeter(id);
    boolean zipCodeExists = parkingMeterRepository.existsByAddress_ZipCode(parkingMeterForm.address().zipCode());
    if (zipCodeExists && !parkingMeter.getAddress().getZipCode().equals(parkingMeterForm.address().zipCode())){
      throw new ParkingMeterException(
              "Parking meter with ZIP code "
                      + parkingMeterForm.address().zipCode()
                      + " already exists.",
              HttpStatus.CONFLICT);
    }
    var updatedParkingMeter = populateParkingMeter(id, parkingMeterForm);
    return this.converterToDTO.toDto(this.parkingMeterRepository.save(updatedParkingMeter));
  }

  @Override
  public void deleteById(String id) {
    log.info("Delete Parking Meter");
    this.parkingMeterRepository.deleteById(id);
  }

  @Override
  @Cacheable(value = "rankedParkingMetersByDate", key = "#startDate + '_' + #endDate")
  @Transactional(readOnly = true)
  public Slice<ParkingMeterCollectionDTO> getHighestEarningParkingMeter(
      String startDate, String endDate, Pageable pageable) {

    Instant startInstant = Instant.parse(startDate);
    Instant endInstant = Instant.parse(endDate);

    Aggregation aggregation =
        Aggregation.newAggregation(
            Aggregation.match(Criteria.where("start_time").gte(startInstant).lte(endInstant)),
            Aggregation.group(PARKING_METER_ID)
                .first("parkingMeter")
                .as("parkingMeter")
                .sum("total_collected_amount")
                .as("totalCollected"),
            Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalCollected")),
            Aggregation.skip(pageable.getOffset()),
            Aggregation.limit(pageable.getPageSize()),
                Aggregation.project()
                        .and("_id").as(PARKING_METER_ID)
                        .and("parkingMeter.operating_hours").as("parkingMeter.operatingHours")
                        .and("parkingMeter.rate").as("parkingMeter.rate")
                        .and("parkingMeter.available_spaces").as("parkingMeter.availableSpaces")
                .and("parkingMeter.address").as("parkingMeter.address")
                .and("parkingMeter.version").as("parkingMeter.version")
                .and("totalCollected").as("totalCollected")
        );

    AggregationResults<ParkingMeterCollectionDTO> results =
        mongoTemplate.aggregate(aggregation, "ticket", ParkingMeterCollectionDTO.class);

    List<ParkingMeterCollectionDTO> resultList =
        results.getMappedResults().stream().filter(Objects::nonNull).collect(Collectors.toList());

    boolean hasNext = resultList.size() == pageable.getPageSize();

    return new SliceImpl<>(resultList, pageable, hasNext);
  }

  @Override
  @Transactional(readOnly = true)
  public ParkingSpaceDTO getAvailableSpace(String id, LocalDate date) {
    log.info("Get Available Spaces");

    ParkingMeter parkingMeter = getParkingMeter(id);

    MatchOperation matchParkingMeter = Aggregation.match(Criteria.where("_id").is(id));

    LookupOperation lookupOperation =
        Aggregation.lookup("ticket", "_id", PARKING_METER_ID, TICKETS);

    UnwindOperation unwindTickets = Aggregation.unwind(TICKETS, INDEX, Boolean.TRUE);

    MatchOperation matchPendingTickets =
        Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where(INDEX).is(null),
                    new Criteria()
                        .andOperator(
                            Criteria.where("tickets.payment_status").is(PaymentStatusEnum.PENDING),
                            Criteria.where("tickets.start_time")
                                .gte(date.atStartOfDay())
                                .lte(date.plusDays(1).atStartOfDay()))));

    GroupOperation groupTickets =
        Aggregation.group("tickets.parkingMeter._id")
            .first("available_spaces")
            .as("spaces")
            .first("address")
            .as("address")
            .first(INDEX)
            .as("idx")
            .count()
            .as("occupied");

    ProjectionOperation projectFields =
        Aggregation.project("_id", "address", "spaces", "idx")
            .and(AggregationSpELExpression.expressionOf("cond(idx==null, spaces, spaces-occupied)"))
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
      parkingSpaceDTO.setAddress(converterToDTO.toDto(parkingMeter.getAddress()));
      parkingSpaceDTO.setSpaces(parkingMeter.getAvailableSpaces());
      parkingSpaceDTO.setAvailable(0);
    } else {
      parkingSpaceDTO = listResult.get(0);
      parkingSpaceDTO.setDate(LocalDateTime.now());
    }

    return parkingSpaceDTO;
  }

  @Override
  @Transactional(readOnly = true)
  public TimesParkedDTO getTimesParkedWithDateRange(
      String parkingMeterId, String licensePlate, LocalDate startDate, LocalDate endDate) {

    getParkingMeter(parkingMeterId);

    if (endDate == null) {
      endDate = LocalDate.now();
    }

    if (startDate.isAfter(endDate)) {
      throw new ParkingMeterException(
              "Start date is greater than end date", HttpStatus.BAD_REQUEST);
    }

    MatchOperation matchParkingMeter = Aggregation.match(Criteria.where("_id").is(parkingMeterId));

    LookupOperation lookupOperation =
        Aggregation.lookup("ticket", "_id", PARKING_METER_ID, TICKETS);

    UnwindOperation unwindTickets = Aggregation.unwind(TICKETS, INDEX, Boolean.TRUE);

    MatchOperation matchPendingTickets =
        Aggregation.match(
            new Criteria()
                .orOperator(
                    Criteria.where(INDEX).is(null),
                    Criteria.where("tickets.vehicle.license_plate").is(licensePlate),
                    Criteria.where("tickets.start_time").gte(startDate.atStartOfDay()).lte(endDate.plusDays(1).atStartOfDay())));

    GroupOperation groupTickets =
        Aggregation.group("tickets.vehicle._id").first(INDEX).as("idx").count().as("times_parked");

    ProjectionOperation projectFields =
        Aggregation.project("_id", "idx", "times_parked")
            .and(AggregationSpELExpression.expressionOf("cond(idx==null, 0, times_parked)"))
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

    List<TimesParkedDTO> listResult = results.getMappedResults();

    TimesParkedDTO timesParkedDTO = new TimesParkedDTO();
    if (listResult.isEmpty()) {
      timesParkedDTO.setTimesParked(0);
    } else {
      timesParkedDTO = listResult.get(0);
    }

    return timesParkedDTO;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ParkingMeterDTO> findAllByCityOrNeighborhood(
      String city, String neighborhood, Pageable pageable) {
    if (ObjectUtils.isEmpty(city) && ObjectUtils.isEmpty(neighborhood)) {
      throw new ParkingMeterException(
              "At least one filter parameter city or neighborhood must be entered", HttpStatus.BAD_REQUEST);
    }

    return parkingMeterRepository
        .findAllByCityAndOrNeighborhood(city, neighborhood, pageable)
        .map(converterToDTO::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public AmountEarnedDTO getParkingMeterEarningsWithDateRange(
      String parkingMeterId, LocalDate startDate, LocalDate endDate) {
    var parkingMeter = getParkingMeter(parkingMeterId);

    if (endDate == null) {
      endDate = LocalDate.now();
    }

    if (startDate.isAfter(endDate)) {
      throw new ParkingMeterException(
          "Start date is greater than end date", HttpStatus.BAD_REQUEST);
    }

    MatchOperation matchParkingMeter = Aggregation.match(Criteria.where("_id").is(parkingMeterId));

    LookupOperation lookupOperation =
        Aggregation.lookup("ticket", "_id", PARKING_METER_ID, TICKETS);

    UnwindOperation unwindTickets = Aggregation.unwind(TICKETS, INDEX, Boolean.TRUE);

    MatchOperation matchPendingTickets =
        Aggregation.match(
            new Criteria()
                .andOperator(
                    Criteria.where("tickets.payment_status").is(PaymentStatusEnum.PAID),
                    Criteria.where("tickets.start_time")
                        .gte(startDate.atStartOfDay())
                        .lt(endDate.plusDays(1))));

    GroupOperation groupTickets =
        Aggregation.group("_id").sum("tickets.total_amount_charged").as("earned");

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
            .addressDTO(converterToDTO.toDto(parkingMeter.getAddress()))
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
  public Page<AmountEarnedByLocalityDTO> getParkingMeterEarningsWithDateRangeByLocality(
      String city, String neighborhood, LocalDate startDate, LocalDate endDate, Pageable pageable) {
    if (ObjectUtils.isEmpty(city) && ObjectUtils.isEmpty(neighborhood)) {
      throw new ParkingMeterException(
          "At least one filter parameter city or neighborhood must be entered", HttpStatus.BAD_REQUEST);
    }

    if (endDate == null) {
      endDate = LocalDate.now();
    }

    if (startDate.isAfter(endDate)) {
      throw new ParkingMeterException(
          "Start date is greater than end date", HttpStatus.BAD_REQUEST);
    }

    List<Criteria> criteriaList = new ArrayList<>();
    if (!ObjectUtils.isEmpty(city)) {
      criteriaList.add(Criteria.where("address.city").is(city));
    }

    if (!ObjectUtils.isEmpty(neighborhood)) {
      criteriaList.add(Criteria.where("address.neighborhood").is(neighborhood));
    }

    MatchOperation matchParkingMeter =
        Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

    LookupOperation lookupOperation =
        Aggregation.lookup("ticket", "_id", PARKING_METER_ID, TICKETS);

    UnwindOperation unwindTickets = Aggregation.unwind(TICKETS, INDEX, Boolean.TRUE);

    MatchOperation matchPendingTickets =
        Aggregation.match(
            new Criteria()
                .andOperator(
                    Criteria.where("tickets.payment_status").is(PaymentStatusEnum.PAID),
                    Criteria.where("tickets.start_time")
                        .gte(startDate.atStartOfDay())
                        .lt(endDate.plusDays(1))));

    GroupOperation groupTickets =
        Aggregation.group("_id")
            .first("address")
            .as("address")
            .sum("tickets.total_amount_charged")
            .as("earned");

    ProjectionOperation projectFields =
        Aggregation.project("address", "earned").and("_id").as("id");

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

  private ParkingMeter populateParkingMeter(String id, ParkingMeterForm parkingMeterForm) {
    log.info("Building object Parking Meter");
    validateOperatingHours(parkingMeterForm.operatingHours());
    return new ParkingMeter(
        id,
        OperationHours.builder()
            .start(parkingMeterForm.operatingHours().start())
            .end(parkingMeterForm.operatingHours().end())
            .build(),
        Rate.builder()
            .firstHour(parkingMeterForm.rate().firstHour())
            .additionalHours(parkingMeterForm.rate().additionalHours())
            .build(),
        parkingMeterForm.availableSpaces(),
        Address.builder()
            .complement(parkingMeterForm.address().complement())
            .street(parkingMeterForm.address().street())
            .neighborhood(parkingMeterForm.address().neighborhood())
            .city(parkingMeterForm.address().city())
            .state(parkingMeterForm.address().state())
            .zipCode(parkingMeterForm.address().zipCode())
            .number(parkingMeterForm.address().number())
            .build(),
        id != null ? this.getParkingMeter(id).getVersion() : null);
  }

  private void validateOperatingHours(OperatingHoursParkingForm operatingHours) {
    log.info("Validating Operating Hours");
    LocalTime startTime;
    LocalTime endTime;

    try {
      String[] slicedHour = operatingHours.start().split(":");
      startTime = LocalTime.of(Integer.parseInt(slicedHour[0]), Integer.parseInt(slicedHour[1]));

      slicedHour = operatingHours.end().split(":");
      endTime = LocalTime.of(Integer.parseInt(slicedHour[0]), Integer.parseInt(slicedHour[1]));
    } catch (Exception e) {
      throw new ParkingMeterException(
          "Problem with Operating Hours: check if it was filled correctly in HH:mm format",
          HttpStatus.BAD_REQUEST);
    }

    if (startTime.isAfter(endTime)) {
      throw new ParkingMeterException(
          "Start time is greater than end time", HttpStatus.BAD_REQUEST);
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
