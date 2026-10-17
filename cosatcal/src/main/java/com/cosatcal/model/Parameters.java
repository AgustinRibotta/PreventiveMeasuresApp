package com.cosatcal.model;

import java.math.BigDecimal;

public record Parameters(
        double scrapPct,
        double materialMarkupPct,
        double gasMarkupPct,
        BigDecimal machineCostPerHourEur,
        BigDecimal gasPackCostEur,
        double gasPackM3) {}
