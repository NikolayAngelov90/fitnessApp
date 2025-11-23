package com.fitnessapp.reports.client.config;

import com.fitnessapp.exception.MembershipReportNotFoundException;
import com.fitnessapp.exception.WorkoutReportNotFoundException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class CustomErrorDecoderUTest {

    private final CustomErrorDecoder decoder = new CustomErrorDecoder();

    private static Response response(int status, String url) {
        Request req = Request.create(Request.HttpMethod.GET, url, Collections.emptyMap(), null, new RequestTemplate());
        return Response.builder()
                .status(status)
                .reason("reason")
                .request(req)
                .headers(Collections.emptyMap())
                .body(new byte[0])
                .build();
    }

    @Test
    void decode_404WorkoutReports_returnsWorkoutReportNotFound() {
        Exception ex = decoder.decode("getWorkoutReport", response(404, "http://host/workout-reports/by-type"));
        assertTrue(ex instanceof WorkoutReportNotFoundException);
        assertEquals("Report for workouts not found", ex.getMessage());
    }

    @Test
    void decode_404MembershipReports_returnsMembershipReportNotFound() {
        Exception ex = decoder.decode("getMembershipReport", response(404, "http://host/membership-reports/monthly"));
        assertTrue(ex instanceof MembershipReportNotFoundException);
        assertEquals("Report for memberships not found", ex.getMessage());
    }

    @Test
    void decode_otherStatus_delegatesToDefaultDecoder() {
        Exception ex = decoder.decode("any", response(500, "http://host/other"));
        // Default decoder should create a FeignException for non-404
        assertTrue(ex instanceof FeignException);
    }
}
