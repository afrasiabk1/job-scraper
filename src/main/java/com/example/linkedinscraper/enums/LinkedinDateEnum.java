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
    public static LinkedinDateEnum getLinkedinEnum(String name){
        if (pastDay.name().equals(name)){
            return pastDay;
        }
        else if (pastMonth.name().equals(name)){
            return pastMonth;
        }
        else if (pastWeek.name().equals(name)){
            return pastWeek;
        }
        return null;
    }
}
