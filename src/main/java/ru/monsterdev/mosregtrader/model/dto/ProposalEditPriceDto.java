package ru.monsterdev.mosregtrader.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;
import ru.monsterdev.mosregtrader.utils.converters.DateDeserializers;
import ru.monsterdev.mosregtrader.utils.converters.DateSerializers;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalEditPriceDto {

  @JsonProperty("Id")
  private Long id;
  @JsonProperty("TradeId")
  private Long tradeId;
  @JsonProperty("TradeName")
  private String tradeName;
  @JsonProperty("FillingApplicationEndDate")
  @JsonDeserialize(using = DateDeserializers.LocalDateTimeDeserializer.class)
  @JsonSerialize(using = DateSerializers.LocalDateTimeSerializer.class)
  private LocalDateTime fillEndDate;
  //@JsonProperty("PlanedDealSignDate")
  //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "Europe/Moscow")
  //private String signDate;
  @JsonProperty("InitialPrice")
  BigDecimal initialPrice;
  @JsonProperty("Products")
  private Set<ProductDto> products;
  @JsonProperty("Price")
  private BigDecimal price;
}
