package ru.monsterdev.mosregtrader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ContactInfo {
  @JsonProperty("ContactInfoLastName")
  private String lastName;

  @JsonProperty("ContactInfoFirstName")
  private String firstName;

  @JsonProperty("ContactInfoMiddleName")
  private String fatherName;

  @JsonProperty("ContactInfoPhone")
  private String phone;

  @JsonProperty("ContactInfoFax")
  private String fax;

  @JsonProperty("ContactInfoEmail")
  private String email;
}
