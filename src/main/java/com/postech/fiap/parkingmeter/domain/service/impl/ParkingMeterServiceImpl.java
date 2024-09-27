package com.postech.fiap.parkingmeter.domain.service.impl;

import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;
import com.postech.fiap.parkingmeter.domain.model.dto.ParkingMeterDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.ParkingMeterForm;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Endereco;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.HorarioFuncionamento;
import com.postech.fiap.parkingmeter.domain.repository.ParkingMeterRepository;
import com.postech.fiap.parkingmeter.domain.service.ParkingMeterService;
import com.postech.fiap.parkingmeter.domain.util.ConverterToDTO;
import com.postech.fiap.parkingmeter.infrastructure.exception.ParkingMeterException;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
@Slf4j
@ImportAutoConfiguration(TransactionAutoConfiguration.class)
public class ParkingMeterServiceImpl implements ParkingMeterService {

  @Autowired private final ParkingMeterRepository parkingMeterRepository;
  private final ConverterToDTO converterToDTO;

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
    log.info("Create Parking Meter");
    var parkingMeter = preencherParkingMeter(null, parkingMeterForm);
    return converterToDTO.toDto(this.parkingMeterRepository.save(parkingMeter));
  }

  @Override
  @Transactional
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
      throw new ParkingMeterException("problem with the HorarioFuncionamento check if it was filled correctly HH:mm", HttpStatus.BAD_REQUEST);
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
