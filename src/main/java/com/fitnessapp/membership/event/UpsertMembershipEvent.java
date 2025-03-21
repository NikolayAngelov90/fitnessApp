package com.fitnessapp.membership.event;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
public class UpsertMembershipEvent {

    private LocalDate date;

    private String type;

    private BigDecimal price;
}
