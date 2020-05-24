package ru.monsterdev.mosregtrader.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Data;
import ru.monsterdev.mosregtrader.enums.VatRateState;
import ru.monsterdev.mosregtrader.model.ContactInfo;

@Data
public class PublishProposalDto {
  @JsonProperty("ContactInfo")
  private ContactInfo contactInfo;
  @JsonProperty("ApplicationDocuments")
  private List applicationDocuments = new ArrayList();
  @JsonProperty("AgreeWithCustomerConditions")
  private boolean agreeWithCustomerConditions = true;
  @JsonProperty("DefineCustomPriceForEachProduct")
  private boolean isDefineCustomPriceForEachProduct = true;
  @JsonProperty("Price")
  private String price;
  @JsonProperty("IncludeVatRate")
  private boolean includeVatRate;
  @JsonProperty("IncludeVatRateChecked")
  private boolean includeVatRateChecked;
  @JsonProperty("VatRateState")
  private VatRateState vatRateState;
  @JsonProperty("VatRate")
  private Integer vatRate;
  @JsonProperty("OfferExpiryDate")
  private String offerExpiryDate = null;
  @JsonProperty("NeverExpired")
  private boolean neverExpired = true;
  @JsonProperty("Id")
  private Integer id = 0;
  @JsonProperty("Products")
  private Set<ProductDto> products;
}
