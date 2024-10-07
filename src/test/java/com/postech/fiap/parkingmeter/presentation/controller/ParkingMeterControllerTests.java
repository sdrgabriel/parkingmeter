package com.postech.fiap.parkingmeter.presentation.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.postech.fiap.parkingmeter.domain.model.dto.ParkingMeterDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.ParkingMeterForm;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Address;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.OperatingHours;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Tarifa;
import com.postech.fiap.parkingmeter.domain.service.ParkingMeterService;
import java.util.*;
import org.assertj.core.api.Assertions;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ParkingMeterControllerTests {

    private final List<ParkingMeterDTO> listOfParkingMeterDTO = new ArrayList<>();
    private final String
            ID = new ObjectId("1".repeat(24)).toString(),
            KEY_FORM = "ParkingMeterForm",
            KEY_DTO = "ParkingMeterDTO";
    private final Map<String, Object> testData = createTestData();

    @Mock
    private ParkingMeterService parkingMeterService;

    @Test
    void givenPagebleObject_whenFindAllWithNoResult_thenReturnPageWithNoContent() {
        Page<ParkingMeterDTO> expectedPage = new PageImpl<>(listOfParkingMeterDTO);

        when(parkingMeterService.findAll(any(Pageable.class))).thenReturn(expectedPage);

        var unit = initUnit();
        var actualPage = unit.findAll(Pageable.unpaged());

        assertThat(actualPage).isNotNull();
        assertThat(Objects.requireNonNull(actualPage.getBody()).getContent()).isEqualTo(List.of(new ParkingMeterDTO[0]));
        assertThat(actualPage.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualPage.getBody()).isEqualTo(expectedPage);
        verify(parkingMeterService).findAll(Pageable.unpaged());
    }

    @Test
    void givenPageableObject_whenFindAllWithResult_thenReturnPageWithTwoParkingMeterDTO() {
        var expectePageavle = PageRequest.of(2, 10);
        var parkingMeterDTO = (ParkingMeterDTO) testData.get(KEY_DTO);
        listOfParkingMeterDTO.add(parkingMeterDTO);
        listOfParkingMeterDTO.add(parkingMeterDTO);
        Page<ParkingMeterDTO> expectedPage = new PageImpl<>(listOfParkingMeterDTO);

        when(parkingMeterService.findAll(any(Pageable.class))).thenReturn(expectedPage);

        var unit = initUnit();
        var actualPage = unit.findAll(expectePageavle);

        assertThat(actualPage).isNotNull();
        assertThat(Objects.requireNonNull(actualPage.getBody()).getContent().size()).isEqualTo(2);
        assertThat(actualPage.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualPage.getBody()).isEqualTo(expectedPage);
        verify(parkingMeterService).findAll(expectePageavle);
    }

    @Test
    void givenIdObject_whenGetByIdWithNoMatching_thenReturnNull() {
        var unit = initUnit();
        var actualResult = unit.getById(ID);

        assertThat(actualResult).isNotNull();
        Assertions.assertThat(actualResult.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(actualResult.getBody()).isEqualTo(null);
        verify(parkingMeterService).getById(ID);
    }

    @Test
    void givenIdObject_whenGetByIdWithMatching_thenReturnDTO() {
        var expectedDTO = (ParkingMeterDTO) testData.get(KEY_DTO);

        when(parkingMeterService.getById(anyString())).thenReturn(expectedDTO);

        var unit = initUnit();
        var acutalDTO = unit.getById(ID);

        assertThat(acutalDTO).isNotNull();
        assertThat(acutalDTO.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(acutalDTO.getBody()).isEqualTo(expectedDTO);
        assertThat(Objects.requireNonNull(acutalDTO.getBody()).getId()).isEqualTo(ID);
        verify(parkingMeterService).getById(ID);
    }

    @Test
    void givenParkingMeterFormObject_whenCreateValid_thenReturnDTO() {
        var parkingMeterForm = (ParkingMeterForm) testData.get(KEY_FORM);
        var expectedDTO = (ParkingMeterDTO) testData.get(KEY_DTO);

        when(parkingMeterService.create(any(ParkingMeterForm.class))).thenReturn(expectedDTO);

        var unit = initUnit();
        var acutalDTO = unit.create(parkingMeterForm);

        assertThat(acutalDTO).isNotNull();
        assertThat(acutalDTO.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(acutalDTO.getBody()).isEqualTo(expectedDTO);
        assertThat(Objects.requireNonNull(acutalDTO.getBody()).getId()).isEqualTo(ID);
        verify(parkingMeterService).create(parkingMeterForm);
    }

    @Test
    void givenIdObkectAndParkingMeterFormObject_whenIsValidToUpdate_thenReturnUpdatedDTO() {
        var newHorarioFuncionamento = new OperatingHours("01:00", "12:00");
        var parkingMeterForm = (ParkingMeterForm) testData.get(KEY_FORM);
        var parkingMeterFormMod = new ParkingMeterForm(
                newHorarioFuncionamento,
                parkingMeterForm.tarifa(),
                parkingMeterForm.vagasDisponiveis(),
                parkingMeterForm.cep()
        );
        var expectedDTO = (ParkingMeterDTO) testData.get(KEY_DTO);
        expectedDTO.setHorarioFuncionamento(newHorarioFuncionamento);

        when(parkingMeterService.updateById(anyString(), any(ParkingMeterForm.class))).thenReturn(expectedDTO);

        var unit = initUnit();
        var acutalDTO = unit.updateById(ID, parkingMeterFormMod);

        assertThat(acutalDTO).isNotNull();
        assertThat(acutalDTO.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(acutalDTO.getBody()).isEqualTo(expectedDTO);
        assertThat(Objects.requireNonNull(acutalDTO.getBody()).getId()).isEqualTo(ID);
        verify(parkingMeterService).updateById(ID, parkingMeterFormMod);
    }

    @Test
    void givenIdObject_whenDeleteByIDValid_thenReturnResponseWithNoContent() {
        var unit = initUnit();
        var acutalDTO = unit.deleteById(ID);

        assertThat(acutalDTO).isNotNull();
        assertThat(acutalDTO.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(acutalDTO.getBody()).isNull();
        verify(parkingMeterService).deleteById(ID);
    }

    private Map<String, Object> createTestData() {
        final var HORARIO_FUNCIONAMENTO = new OperatingHours("05:00", "15:00");
        final var TARIFA = new Tarifa(5, 10);
        final var VAGAS_DISPONIVEIS = 7;
        final var ENDERECO = new Address(
                "Rua Domingos Barreto",
                "Jardim Everest",
                "São Paulo",
                "São Paulo",
                "05601-030"
        );
        Map<String, Object> datas = new HashMap<>();
        datas.put(KEY_FORM, new ParkingMeterForm(
                HORARIO_FUNCIONAMENTO,
                TARIFA,
                VAGAS_DISPONIVEIS,
                ENDERECO.cep()
        ));
        datas.put(KEY_DTO, new ParkingMeterDTO(
                ID,
                HORARIO_FUNCIONAMENTO,
                TARIFA,
                VAGAS_DISPONIVEIS,
                ENDERECO,
                0L
        ));
        return datas;
    }

    private ParkingMeterController initUnit() {
        return new ParkingMeterController(parkingMeterService);
    }
}
