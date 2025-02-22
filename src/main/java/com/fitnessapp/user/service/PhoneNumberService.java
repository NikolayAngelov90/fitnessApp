package com.fitnessapp.user.service;

import com.fitnessapp.exception.InvalidPhoneNumberException;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class PhoneNumberService {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    public PhoneNumber parsePhoneNumber(String number, String regionCode) {
        try {
            return phoneUtil.parse(number, regionCode);
        } catch (NumberParseException e) {
            String errorMessage= switch (e.getErrorType()) {
                case INVALID_COUNTRY_CODE ->  "Invalid Country Code";
                case NOT_A_NUMBER ->   "Invalid number format";
                default -> "Invalid phone number";
            };
            throw new InvalidPhoneNumberException(errorMessage);
        }
    }

    public boolean isValidNumber(PhoneNumber phoneNumber) {
        return phoneUtil.isValidNumber(phoneNumber);
    }

    public String formatE164(PhoneNumber phoneNumber) {
        return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    }
}
