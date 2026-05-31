package com.hustsimulator.context.enums;

import com.hustsimulator.context.common.DashboardConstants;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public enum TimeRangeFilter {
    ONE_DAY("1d", DashboardConstants.TIME_FORMAT_HOURLY, "hour"),
    ONE_WEEK("1w", DashboardConstants.TIME_FORMAT_DAILY, "day"),
    ONE_MONTH("1m", DashboardConstants.TIME_FORMAT_DAILY, "day"),
    ONE_YEAR("1y", DashboardConstants.TIME_FORMAT_MONTHLY, "month"),
    ALL("all", DashboardConstants.TIME_FORMAT_MONTHLY, "month");

    private final String code;
    private final String format;
    private final String bucketType;

    TimeRangeFilter(String code, String format, String bucketType) {
        this.code = code;
        this.format = format;
        this.bucketType = bucketType;
    }

    public LocalDateTime getSince(LocalDateTime now) {
        switch (this) {
            case ONE_WEEK: return now.minusWeeks(1);
            case ONE_MONTH: return now.minusMonths(1);
            case ONE_YEAR: return now.minusYears(1);
            case ALL: return now.minusYears(10);
            case ONE_DAY:
            default: return now.minusHours(24);
        }
    }

    public static TimeRangeFilter fromCode(String code) {
        if (code == null) return ONE_DAY;
        for (TimeRangeFilter filter : values()) {
            if (filter.code.equals(code)) return filter;
        }
        return ONE_DAY;
    }
}
