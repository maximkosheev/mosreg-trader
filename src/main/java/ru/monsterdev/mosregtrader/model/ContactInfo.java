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

  @JsonProperty("ContactInfoBankBik")
  private String bik;

  @JsonProperty("ContactInfoBankCheckingAccount")
  private String checkingAccount;

  @JsonProperty("ContactInfoBankCorrespondentAccount")
  private String correspondentAccount;

  @JsonProperty("ContactInfoBankName")
  private String bankName;

  @JsonProperty("ContactInfoBankPersonalAccount")
  private String personalAccount;
}
