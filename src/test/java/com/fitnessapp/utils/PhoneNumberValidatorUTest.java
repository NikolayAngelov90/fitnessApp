package com.fitnessapp.utils;

import com.fitnessapp.exception.InvalidPhoneNumberException;
import com.fitnessapp.user.service.PhoneNumberService;
import com.fitnessapp.utils.Validation.PhoneNumberValidator;
import com.fitnessapp.utils.Validation.ValidPhoneNumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PhoneNumberValidatorUTest {

    @Mock
    private PhoneNumberService phoneNumberService;
    @Mock
    private ValidPhoneNumber validPhoneNumberAnnotation;
    @Mock
    private ConstraintValidatorContext constraintValidatorContext;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    private PhoneNumberValidator phoneNumberValidator;

    private final String testRegion = "BG";

    @BeforeEach
    void setUp() {
        phoneNumberValidator = new PhoneNumberValidator(phoneNumberService);

        lenient().when(validPhoneNumberAnnotation.region()).thenReturn(testRegion);
        phoneNumberValidator.initialize(validPhoneNumberAnnotation);

        lenient().when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(constraintViolationBuilder);
    }

    @Test
    void givenNullValue_whenIsValid_thenReturnTrue() {
        // When
        boolean result = phoneNumberValidator.isValid(null, constraintValidatorContext);

        // Then
        assertTrue(result);
        verifyNoInteractions(phoneNumberService);
        verify(constraintValidatorContext, never()).disableDefaultConstraintViolation();
        verify(constraintValidatorContext, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void givenEmptyValue_whenIsValid_thenReturnTrue() {
        // Given
        String phoneNumberValue = "";

        // When
        boolean result = phoneNumberValidator.isValid(phoneNumberValue, constraintValidatorContext);

        // Then
        assertTrue(result);
        verifyNoInteractions(phoneNumberService);
        verify(constraintValidatorContext, never()).disableDefaultConstraintViolation();
        verify(constraintValidatorContext, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void givenValidNumber_whenIsValid_thenReturnTrue() throws InvalidPhoneNumberException {
        // Given
        String phoneNumberValue = "+359888123456";
        PhoneNumber parsedNumber = new PhoneNumber();

        when(phoneNumberService.parsePhoneNumber(phoneNumberValue, testRegion)).thenReturn(parsedNumber);
        when(phoneNumberService.isValidNumber(parsedNumber)).thenReturn(true);

        // When
        boolean result = phoneNumberValidator.isValid(phoneNumberValue, constraintValidatorContext);

        // Then
        assertTrue(result);

        verify(phoneNumberService).parsePhoneNumber(phoneNumberValue, testRegion);
        verify(phoneNumberService).isValidNumber(parsedNumber);
        verify(constraintValidatorContext, never()).disableDefaultConstraintViolation();
        verify(constraintValidatorContext, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void givenParsableButInvalidNumber_whenIsValid_thenReturnFalseAndSetDefaultMessage() throws InvalidPhoneNumberException {
        //
        String phoneNumberValue = "+359123";
        PhoneNumber parsedNumber = new PhoneNumber();

        when(phoneNumberService.parsePhoneNumber(phoneNumberValue, testRegion)).thenReturn(parsedNumber);
        when(phoneNumberService.isValidNumber(parsedNumber)).thenReturn(false);

        // When
        boolean result = phoneNumberValidator.isValid(phoneNumberValue, constraintValidatorContext);

        // Then
        assertFalse(result);

        verify(phoneNumberService).parsePhoneNumber(phoneNumberValue, testRegion);
        verify(phoneNumberService).isValidNumber(parsedNumber);
        verify(constraintValidatorContext).disableDefaultConstraintViolation();
        verify(constraintValidatorContext).buildConstraintViolationWithTemplate("Invalid phone number");
        verify(constraintViolationBuilder).addConstraintViolation();
    }

    @Test
    void givenUnparsableNumberFormatException_whenIsValid_thenReturnFalseAndSetExceptionMessage() throws InvalidPhoneNumberException {
        // Given
        String phoneNumberValue = "not is number";
        String expectedErrorMessage = "Invalid number format";

        when(phoneNumberService.parsePhoneNumber(phoneNumberValue, testRegion))
                .thenThrow(new InvalidPhoneNumberException(expectedErrorMessage));

        // When
        boolean result = phoneNumberValidator.isValid(phoneNumberValue, constraintValidatorContext);

        // Then
        assertFalse(result);

        verify(phoneNumberService).parsePhoneNumber(phoneNumberValue, testRegion);
        verify(phoneNumberService, never()).isValidNumber(any(PhoneNumber.class));
        verify(constraintValidatorContext).disableDefaultConstraintViolation();
        verify(constraintValidatorContext).buildConstraintViolationWithTemplate(expectedErrorMessage);
        verify(constraintViolationBuilder).addConstraintViolation();
    }

    @Test
    void givenUnparsableNumberCountryCodeException_whenIsValid_thenReturnFalseAndSetExceptionMessage() throws InvalidPhoneNumberException {
        // Given
        String phoneNumberValue = "+999123";
        String expectedErrorMessage = "Invalid Country Code";

        when(phoneNumberService.parsePhoneNumber(phoneNumberValue, testRegion))
                .thenThrow(new InvalidPhoneNumberException(expectedErrorMessage));

        // When
        boolean result = phoneNumberValidator.isValid(phoneNumberValue, constraintValidatorContext);

        // Then
        assertFalse(result);

        verify(phoneNumberService).parsePhoneNumber(phoneNumberValue, testRegion);
        verify(phoneNumberService, never()).isValidNumber(any(PhoneNumber.class));
        verify(constraintValidatorContext).disableDefaultConstraintViolation();
        verify(constraintValidatorContext).buildConstraintViolationWithTemplate(expectedErrorMessage);
        verify(constraintViolationBuilder).addConstraintViolation();
    }
}
