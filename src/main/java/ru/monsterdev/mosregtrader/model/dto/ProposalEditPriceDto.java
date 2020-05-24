package ru.monsterdev.mosregtrader.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Set;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalEditPriceDto {

  @JsonProperty("Id")
  private Long id;
  @JsonProperty("TradeId")
  private Long tradeId;
  @JsonProperty("TradeName")
  private String tradeName;
  // В это поле передается дата-время при этом может передаваться как с милисекундами так и нет. В этом случае можно
  // было бы воспользоваться шаблоном "yyyy-MM-dd'T'HH:mm:ss[.SSS]X, но такой шаблон будет работать только
  // при десериалации времени, где указаны миллисекунды из 3 цифр, а мне может быть передано и одна, и две, и три цифры
  // поэтому воспользуемся кастомным десериализатором
  /*
  @JsonProperty("FillingApplicationEndDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "Europe/Moscow")
  @JsonDeserialize(using = DateDeserializer.class)
  private Date fillEndDate;
  */
  @JsonProperty("PlanedDealSignDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "Europe/Moscow")
  private String signDate;
  @JsonProperty("InitialPrice")
  BigDecimal initialPrice;
  @JsonProperty("Products")
  private Set<ProductDto> products;
  @JsonProperty("Price")
  private BigDecimal price;
}
