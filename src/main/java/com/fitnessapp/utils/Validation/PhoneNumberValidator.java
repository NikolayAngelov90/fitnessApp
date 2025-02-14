package com.fitnessapp.utils.Validation;

import com.fitnessapp.exception.InvalidPhoneNumberException;
import com.fitnessapp.utils.services.PhoneNumberService;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private String regionCode;
    private final PhoneNumberService phoneNumberService;

    @Autowired
    public PhoneNumberValidator(PhoneNumberService phoneNumberService) {
        this.phoneNumberService = phoneNumberService;
    }

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.regionCode = constraintAnnotation.region();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isEmpty()) return true;

        try {
            PhoneNumber number = phoneNumberService.parsePhoneNumber(value, regionCode);
            if (!phoneNumberService.isValidNumber(number)) {
                serErrorMessage(context, "Invalid phone number");
                return false;
            }

            return true;
        } catch (InvalidPhoneNumberException e) {
            serErrorMessage(context, e.getMessage());
            return false;
        }
    }

    private void serErrorMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
