package com.fitnessapp.reports.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MembershipReport {

    private LocalDate month;

    private int activeCount;

    private int expiredCount;

    private BigDecimal totalRevenue;
}
