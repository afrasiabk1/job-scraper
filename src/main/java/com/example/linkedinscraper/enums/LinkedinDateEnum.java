package com.example.linkedinscraper.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum LinkedinDateEnum {
    pastDay(86400),
    pastWeek(604800),
    pastMonth(2592000);
    private final int val;

    LinkedinDateEnum(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}
