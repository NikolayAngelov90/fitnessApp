package com.fitnessapp.user;

import com.fitnessapp.exception.InvalidPhoneNumberException;
import com.fitnessapp.user.service.PhoneNumberService;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PhoneNumberServiceUTest {

    private PhoneNumberService phoneNumberService;

    private PhoneNumber validBulgarianNumber;
    private PhoneNumber invalidButParsableNumber;
    private PhoneNumber validUkNumber;

    @BeforeEach
    void setUp() throws NumberParseException {

        phoneNumberService = new PhoneNumberService();

        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        validBulgarianNumber = util.parse("+359888123456", "BG");
        invalidButParsableNumber = util.parse("+359123", "BG");
        validUkNumber = util.parse("+442071234567", "GB");
    }

    @Test
    void givenValidNationalNumberAndRegion_whenParsePhoneNumber_thenReturnPhoneNumber() {
        // Given
        String number = "0888123456";
        String regionCode = "BG";

        // When
        PhoneNumber result = phoneNumberService.parsePhoneNumber(number, regionCode);

        // Then
        assertNotNull(result);
        assertEquals(359, result.getCountryCode());
        assertEquals(888123456L, result.getNationalNumber());
    }

    @Test
    void givenValidInternationalNumberAndRegion_whenParsePhoneNumber_thenReturnPhoneNumber() {
        // Given
        String number = "+359888123456";
        String regionCode = "BG";

        // When
        PhoneNumber result = phoneNumberService.parsePhoneNumber(number, regionCode);

        // Then
        assertNotNull(result);
        assertEquals(359, result.getCountryCode());
        assertEquals(888123456L, result.getNationalNumber());
    }

    @Test
    void givenValidInternationalNumberAndNullRegion_whenParsePhoneNumber_thenReturnPhoneNumber() {
        // Given
        String number = "+442071234567";

        // When
        PhoneNumber result = phoneNumberService.parsePhoneNumber(number, null);

        // Then
        assertNotNull(result);
        assertEquals(44, result.getCountryCode());
        assertEquals(2071234567L, result.getNationalNumber());
    }

    @Test
    void givenInvalidNumberFormat_whenParsePhoneNumber_thenThrowInvalidPhoneNumberExceptionWithSpecificMessage() {
        // Given
        String number = "not is number";
        String regionCode = "BG";

        // When & Then
        InvalidPhoneNumberException exception = assertThrows(InvalidPhoneNumberException.class,
                () -> phoneNumberService.parsePhoneNumber(number, regionCode));

        assertEquals("Invalid number format", exception.getMessage());
    }

    @Test
    void givenInvalidCountryCode_whenParsePhoneNumber_thenThrowInvalidPhoneNumberExceptionWithSpecificMessage() {
        // Given
        String number = "+9991234567";

        // When & Then
        InvalidPhoneNumberException exception = assertThrows(InvalidPhoneNumberException.class,
                () -> phoneNumberService.parsePhoneNumber(number, null));

        assertEquals("Invalid Country Code", exception.getMessage());
    }

    @Test
    void givenNumberTooLong_whenParsePhoneNumber_thenThrowInvalidPhoneNumberExceptionWithDefaultMessage() {
        // Given
        String number = "+359888111222333444555";
        String regionCode = "BG";

        // When & Then
        InvalidPhoneNumberException exception = assertThrows(InvalidPhoneNumberException.class,
                () -> phoneNumberService.parsePhoneNumber(number, regionCode));

        assertEquals("Invalid phone number", exception.getMessage());
    }

    @Test
    void givenValidParsedNumber_whenIsValidNumber_thenReturnTrue() {
        // When
        boolean isValid = phoneNumberService.isValidNumber(validBulgarianNumber);

        // Then
        assertTrue(isValid);
    }

    @Test
    void givenAnotherValidParsedNumber_whenIsValidNumber_thenReturnTrue() {
        // When
        boolean isValid = phoneNumberService.isValidNumber(validUkNumber);

        // Then
        assertTrue(isValid);
    }

    @Test
    void givenInvalidParsedNumber_whenIsValidNumber_thenReturnFalse() {
        // When
        boolean isValid = phoneNumberService.isValidNumber(invalidButParsableNumber);

        // Then
        assertFalse(isValid);
    }

    @Test
    void givenNullPhoneNumber_whenIsValidNumber_thenThrowNullPointerException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> phoneNumberService.isValidNumber(null));
    }

    @Test
    void givenParsedNationalNumber_whenFormatE164_thenReturnE164String() {
        // Given
        String expectedFormat = "+359888123456";

        // When
        String formattedNumber = phoneNumberService.formatE164(validBulgarianNumber);

        // Then
        assertEquals(expectedFormat, formattedNumber);
    }

    @Test
    void givenParsedInternationalNumber_whenFormatE164_thenReturnE164String() {
        // Given
        String expectedFormat = "+442071234567";

        // When
        String formattedNumber = phoneNumberService.formatE164(validUkNumber);

        // Then
        assertEquals(expectedFormat, formattedNumber);
    }

    @Test
    void givenInvalidButParsableNumber_whenFormatE164_thenReturnFormattedString() {
        // Given
        String expectedFormat = "+359123";

        // When
        String formattedNumber = phoneNumberService.formatE164(invalidButParsableNumber);

        // Then
        assertEquals(expectedFormat, formattedNumber);
    }

    @Test
    void givenNullPhoneNumber_whenFormatE164_thenThrowNullPointerException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> phoneNumberService.formatE164(null));
    }
}
