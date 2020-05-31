package ru.monsterdev.mosregtrader.model;

import lombok.Data;

@Data
public class StatusFilterOption {
    private String desciption;
    private int code;

    public StatusFilterOption(int code, String desciption) {
        this.code = code;
        this.desciption = desciption;
    }

    @Override
    public String toString() {
        return desciption;
    }
}
