package ru.monsterdev.mosregtrader.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SourcePlatformType {
  EASUZ(10, "ЕАСУЗ"),
  EXTERNAL(25, "Портал поставщиков");

  private int id;
  private String name;

  SourcePlatformType(int id, String name) {
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
