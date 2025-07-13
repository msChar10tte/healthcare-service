package ru.netology.patient.service.medical;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;

import java.math.BigDecimal;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class MedicalServiceImplTest {

    @Mock
    private PatientInfoRepository patientInfoRepository;

    @Mock
    private SendAlertService sendAlertService;

    @InjectMocks
    private MedicalServiceImpl medicalService;

    @Test
    void testCheckBloodPressure_WhenPressureIsNotNormal_ShouldSendAlert() {
        String patientId = "p123";

        PatientInfo patientInfo = new PatientInfo(
                patientId, "Иван", "Петров",
                LocalDate.of(1980, 11, 20),
                new HealthInfo(new BigDecimal("36.6"), new BloodPressure(120, 80))
        );
        Mockito.when(patientInfoRepository.getById(patientId)).thenReturn(patientInfo);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        medicalService.checkBloodPressure(patientId, new BloodPressure(150, 95));

        Mockito.verify(sendAlertService, Mockito.times(1)).send(messageCaptor.capture());
        String expectedMessage = String.format("Warning, patient with id: %s, need help", patientId);
        Assertions.assertEquals(expectedMessage, messageCaptor.getValue());
    }

    @Test
    void testCheckTemperature_WhenTemperatureIsTooLow_ShouldSendAlert() {
        String patientId = "p456";
        PatientInfo patientInfo = new PatientInfo(
                patientId, "Анна", "Сидорова",
                LocalDate.of(1995, 3, 15),
                new HealthInfo(new BigDecimal("36.8"), new BloodPressure(120, 80))
        );
        Mockito.when(patientInfoRepository.getById(patientId)).thenReturn(patientInfo);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        medicalService.checkTemperature(patientId, new BigDecimal("35.2"));

        Mockito.verify(sendAlertService, Mockito.times(1)).send(messageCaptor.capture());
        String expectedMessage = String.format("Warning, patient with id: %s, need help", patientId);
        Assertions.assertEquals(expectedMessage, messageCaptor.getValue());
    }

    @Test
    void testCheckIndicators_WhenIndicatorsAreNormal_ShouldNotSendAlert() {
        String patientId = "p789";
        BloodPressure normalPressure = new BloodPressure(125, 75);
        BigDecimal normalTemperature = new BigDecimal("36.6");

        PatientInfo patientInfo = new PatientInfo(
                patientId, "Семен", "Горбунков",
                LocalDate.of(1970, 1, 1),
                new HealthInfo(normalTemperature, normalPressure)
        );
        Mockito.when(patientInfoRepository.getById(patientId)).thenReturn(patientInfo);

        medicalService.checkBloodPressure(patientId, new BloodPressure(125, 75));
        medicalService.checkTemperature(patientId, new BigDecimal("36.7"));

        Mockito.verify(sendAlertService, Mockito.never()).send(Mockito.anyString());
    }
}