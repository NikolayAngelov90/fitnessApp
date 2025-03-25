package com.fitnessapp.reports.client.config;

import com.fitnessapp.exception.MembershipReportNotFoundException;
import com.fitnessapp.exception.WorkoutReportNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            if (response.request().url().contains("workout-reports")) {
                return new WorkoutReportNotFoundException("Report for workouts not found");
            } else if (response.request().url().contains("membership-reports")) {
                return new MembershipReportNotFoundException("Report for memberships not found");
            }
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}
