package ru.monsterdev.mosregtrader.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VatRateState {
  USE_NDS(5, "НДС включен"),
  NOT_USE_NSD(10, "НДС не включен");

  private int id;
  private String name;

  VatRateState(int id, String name) {
    this.id = id;
    this.name = name;
  }

  @JsonValue
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
