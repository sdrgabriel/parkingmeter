package com.postech.fiap.parkingmeter.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;
import com.postech.fiap.parkingmeter.domain.model.dto.ParkingMeterDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.ParkingMeterForm;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Endereco;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.HorarioFuncionamento;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Tarifa;
import com.postech.fiap.parkingmeter.domain.repository.ParkingMeterRepository;
import com.postech.fiap.parkingmeter.domain.repository.TicketRepository;
import com.postech.fiap.parkingmeter.domain.util.ConverterToDTO;
import com.postech.fiap.parkingmeter.infrastructure.exception.ParkingMeterException;
import java.util.*;
import org.assertj.core.api.Assertions;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ParkingMeterServiceImplTests {

  private final String ID = new ObjectId("1".repeat(24)).toString(),
      KEY_DOCUMENT = "ParkingMeter",
      KEY_FORM = "ParkingMeterForm",
      KEY_DTO = "ParkingMeterDTO";
  private final Map<String, Object> testData = createTestData();

  @Mock private ParkingMeterRepository parkingMeterRepository;
  @Mock private TicketRepository ticketRepository;
  @Mock private ConverterToDTO converterToDTO;
  @Mock MongoTemplate mongoTemplate;

  @Test
  void givenFormObject_whenCreate_thenReturnDTO() {
    var parkingMeterForm = (ParkingMeterForm) testData.get(KEY_FORM);
    var parkingMeterDTO = (ParkingMeterDTO) testData.get(KEY_DTO);
    var parkingMeter = (ParkingMeter) testData.get(KEY_DOCUMENT);

    when(parkingMeterRepository.save(any(ParkingMeter.class))).thenReturn(parkingMeter);
    when(converterToDTO.toDto(any(ParkingMeter.class))).thenReturn(parkingMeterDTO);

    var unit = initUnit();
    var result = unit.create(parkingMeterForm);

    assertNotNull(result);
    assertThat(result).isEqualTo(parkingMeterDTO);
    verify(parkingMeterRepository).save(parkingMeter);
  }

  @Test
  void givenIdObject_whenIsValid_thenReturnDTO() {
    var parkingMeterDTO = (ParkingMeterDTO) testData.get(KEY_DTO);
    var parkingMeter = (ParkingMeter) testData.get(KEY_DOCUMENT);
    parkingMeter.setId(ID);
    parkingMeter.setVersion(0L);
    Optional<ParkingMeter> optParkingMeter = Optional.of(parkingMeter);

    when(parkingMeterRepository.findById(anyString())).thenReturn(optParkingMeter);
    when(converterToDTO.toDto(any(ParkingMeter.class))).thenReturn(parkingMeterDTO);

    var unit = initUnit();
    var result = unit.getById(ID);

    assertNotNull(result);
    assertThat(result).isEqualTo(parkingMeterDTO);
    verify(parkingMeterRepository).findById(ID);
  }

  @Test
  void givenIdObject_whenIsInvalid_thenReturnException() {
    var expectedMessage = "Parking Meter code does not exist";

    var unit = initUnit();
    var result = assertThrows(ParkingMeterException.class, () -> unit.getById(ID));
    assertNotNull(result);

    var actualMessage = result.getMessage();

    assertSame(expectedMessage, actualMessage);
    assertSame(HttpStatus.NOT_FOUND, result.getStatus());
    verify(parkingMeterRepository).findById(ID);
  }

  @Test
  void givenCepObject_whenGetEnderecoByCepIsValid_thenReturnEndereco() {
    var endereco = ((ParkingMeter) testData.get(KEY_DOCUMENT)).getEndereco();

    var unit = initUnit();
    var result = unit.getEnderecoByCep(endereco.cep());
    assertNotNull(result);

    assertThat(result).isEqualTo(endereco);
  }

  @Test
  void givenCepObject_whenGetEnderecoByCepIsInvalid_thenReturnException() {
    var invalidCep = "1";
    var expectedMessage =
        "Error retrieving address, please evaluate zip code: %s".formatted(invalidCep);

    var unit = initUnit();
    var result = assertThrows(ParkingMeterException.class, () -> unit.getEnderecoByCep(invalidCep));
    assertNotNull(result);

    var actualMessage = result.getMessage();
    assertThat(actualMessage).isEqualTo(expectedMessage);
    assertSame(HttpStatus.BAD_REQUEST, result.getStatus());
  }

  @Test
  void givenIdObject_whenUpdateIsValid_thenReturnUpdatedDTO() {
    var newTarifa = new Tarifa(10, 3.55);

    var parkingMeterForm = (ParkingMeterForm) testData.get(KEY_FORM);
    var parkingMeterFormMod =
        new ParkingMeterForm(
            parkingMeterForm.horarioFuncionamento(),
            newTarifa,
            parkingMeterForm.vagasDisponiveis(),
            parkingMeterForm.cep());
    var parkingMeter = (ParkingMeter) testData.get(KEY_DOCUMENT);

    Optional<ParkingMeter> optParkingMeter = Optional.of(parkingMeter);
    parkingMeter.setId(ID);
    parkingMeter.setTarifa(newTarifa);

    var parkingMeterDTO = (ParkingMeterDTO) testData.get(KEY_DTO);
    parkingMeterDTO.setTarifa(newTarifa);

    when(parkingMeterRepository.findById(anyString())).thenReturn(optParkingMeter);
    when(parkingMeterRepository.save(any(ParkingMeter.class))).thenReturn(parkingMeter);
    when(converterToDTO.toDto(any(ParkingMeter.class))).thenReturn(parkingMeterDTO);

    var unit = initUnit();
    var result = unit.updateById(ID, parkingMeterFormMod);
    assertNotNull(result);

    assertThat(result).isEqualTo(parkingMeterDTO);
    verify(parkingMeterRepository).save(parkingMeter);
  }

  @Test
  void givenIdObject_whenUpdateIsInvalid_thenReturnException() {
    var expectedMessage =
        "problem with the HorarioFuncionamento check if it was filled correctly HH:mm";
    var parkingMeterForm = (ParkingMeterForm) testData.get(KEY_FORM);
    var parkingMeterFormMod =
        new ParkingMeterForm(
            null,
            parkingMeterForm.tarifa(),
            parkingMeterForm.vagasDisponiveis(),
            parkingMeterForm.cep());
    var parkingMeter = (ParkingMeter) testData.get(KEY_DOCUMENT);
    Optional<ParkingMeter> optParkingMeter = Optional.of(parkingMeter);

    when(parkingMeterRepository.findById(anyString())).thenReturn(optParkingMeter);

    var unit = initUnit();
    var result =
        assertThrows(ParkingMeterException.class, () -> unit.updateById(ID, parkingMeterFormMod));
    assertNotNull(result);

    var actualMessage = result.getMessage();
    assertSame(expectedMessage, actualMessage);
    assertSame(HttpStatus.BAD_REQUEST, result.getStatus());
  }

  @Test
  void
      givenIdObject_whenUpdateWithHorarioFuncionamentoWithInicioGretherThanFim_thenReturnException() {
    var expectedMessage = "Start time greater than end time";
    var parkingMeterForm = (ParkingMeterForm) testData.get(KEY_FORM);
    var parkingMeterFormMod =
        new ParkingMeterForm(
            new HorarioFuncionamento("15:00", "05:00"),
            parkingMeterForm.tarifa(),
            parkingMeterForm.vagasDisponiveis(),
            parkingMeterForm.cep());
    var parkingMeter = (ParkingMeter) testData.get(KEY_DOCUMENT);
    Optional<ParkingMeter> optParkingMeter = Optional.of(parkingMeter);

    when(parkingMeterRepository.findById(anyString())).thenReturn(optParkingMeter);

    var unit = initUnit();
    var result =
        assertThrows(ParkingMeterException.class, () -> unit.updateById(ID, parkingMeterFormMod));
    assertNotNull(result);

    var actualMessage = result.getMessage();
    assertSame(expectedMessage, actualMessage);
    assertSame(HttpStatus.BAD_REQUEST, result.getStatus());
  }

  @Test
  void givenIdObject_whenTryDeleteById_thenReturnNothing() {
    var unit = initUnit();
    unit.deleteById(ID);

    verify(parkingMeterRepository).deleteById(ID);
  }

  @Test
  void givenPageableObject_whenFindAll_thenReturnPageOfParkingMeterDTOWithTwoElements() {
    var parkingMeter = (ParkingMeter) testData.get(KEY_DOCUMENT);
    List<ParkingMeter> listOfTwoParkingMeter = new ArrayList<>();
    listOfTwoParkingMeter.add(parkingMeter);
    listOfTwoParkingMeter.add(parkingMeter);
    Page<ParkingMeter> pageOfParkingMeter = new PageImpl<>(listOfTwoParkingMeter);

    var parkingMeterDTO = (ParkingMeterDTO) testData.get(KEY_DTO);
    List<ParkingMeterDTO> listOfTwoParkingMeterDTO = new ArrayList<>();
    listOfTwoParkingMeterDTO.add(parkingMeterDTO);
    listOfTwoParkingMeterDTO.add(parkingMeterDTO);
    Page<ParkingMeterDTO> pageOfParkingMeterDTO = new PageImpl<>(listOfTwoParkingMeterDTO);

    when(parkingMeterRepository.findAll(any(Pageable.class))).thenReturn(pageOfParkingMeter);
    when(converterToDTO.toDto(any(ParkingMeter.class))).thenReturn(parkingMeterDTO);

    var unit = initUnit();
    var result = unit.findAll(Pageable.unpaged());

    assertNotNull(result);
    Assertions.assertThat(result).isEqualTo(pageOfParkingMeterDTO);
    verify(parkingMeterRepository).findAll(Pageable.unpaged());
  }

  private Map<String, Object> createTestData() {
    final var HORARIO_FUNCIONAMENTO = new HorarioFuncionamento("05:00", "15:00");
    final var TARIFA = new Tarifa(5, 10);
    final var VAGAS_DISPONIVEIS = 7;
    final var ENDERECO =
        new Endereco(
            "Rua Domingos Barreto", "Jardim Everest", "São Paulo", "São Paulo", "05601-030");
    Map<String, Object> datas = new HashMap<>();
    datas.put(
        KEY_FORM,
        new ParkingMeterForm(HORARIO_FUNCIONAMENTO, TARIFA, VAGAS_DISPONIVEIS, ENDERECO.cep()));
    datas.put(
        KEY_DOCUMENT,
        new ParkingMeter(null, HORARIO_FUNCIONAMENTO, TARIFA, VAGAS_DISPONIVEIS, ENDERECO, null));
    datas.put(
        KEY_DTO,
        new ParkingMeterDTO(ID, HORARIO_FUNCIONAMENTO, TARIFA, VAGAS_DISPONIVEIS, ENDERECO, 0L));
    return datas;
  }

  private ParkingMeterServiceImpl initUnit() {
    return new ParkingMeterServiceImpl(parkingMeterRepository, converterToDTO, mongoTemplate);
  }
}
