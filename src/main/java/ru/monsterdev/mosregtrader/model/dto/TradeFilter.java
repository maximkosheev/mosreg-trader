package ru.monsterdev.mosregtrader.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import ru.monsterdev.mosregtrader.enums.SourcePlatformType;
import ru.monsterdev.mosregtrader.utils.converters.DateDeserializers;
import ru.monsterdev.mosregtrader.utils.converters.DateSerializers;

@Data
@JsonInclude(Include.NON_EMPTY)
public class TradeFilter {
  @JsonProperty("ClassificatorCodes")
  private List<String> classificatorCodes;

  @JsonProperty("CustomerAddress")
  private String customerAddress;

  @JsonProperty("CustomerFullNameOrInn")
  private String customerFullNameOrInn;

  @JsonProperty("FilterDateFrom")
  @JsonDeserialize(using = DateDeserializers.LocalDateTimeDeserializer.class)
  @JsonSerialize(using = DateSerializers.LocalDateTimeSerializer.class)
  private LocalDateTime filterDateFrom;

  @JsonProperty("FilterDateTo")
  @JsonDeserialize(using = DateDeserializers.LocalDateTimeDeserializer.class)
  @JsonSerialize(using = DateSerializers.LocalDateTimeSerializer.class)
  private LocalDateTime filterDateTo;

  @JsonProperty("FilterFillingApplicationEndDateFrom")
  @JsonDeserialize(using = DateDeserializers.LocalDateTimeDeserializer.class)
  @JsonSerialize(using = DateSerializers.LocalDateTimeSerializer.class)
  private LocalDateTime filterFillingApplicationEndDateFrom;

  @JsonProperty("FilterFillingApplicationEndDateTo")
  @JsonDeserialize(using = DateDeserializers.LocalDateTimeDeserializer.class)
  @JsonSerialize(using = DateSerializers.LocalDateTimeSerializer.class)
  private LocalDateTime filterFillingApplicationEndDateTo;

  @JsonProperty("FilterPriceMin")
  private Long filterPriceMin;

  @JsonProperty("FilterPriceMax")
  private Long filterPriceMax;

  @JsonProperty("FilterTradeEasuzNumber")
  private String filterTradeEasuzNumber;

  @JsonProperty("IsImmediate")
  private Boolean isImmediate;

  @JsonProperty("ItemsPerPage")
  private Integer itemsPerPage;

  @JsonProperty("OnlyTradesWithMyApplications")
  private Boolean onlyTradesWithMyApplications;

  @JsonProperty("Page")
  private Integer page;

  @JsonProperty("ParticipantHasApplicationsOnTrade")
  private String participantHasApplicationsOnTrade;

  @JsonProperty("ShowOnlyOwnTrades")
  private Boolean showOnlyOwnTrades;

  @JsonProperty("sortingParams")
  private List<String> sortingParams;

  @JsonProperty("Id")
  private Long id;

  @JsonProperty("TradeName")
  private String tradeName;

  @JsonProperty("TradeState")
  private Integer tradeState;

  @JsonProperty("UsedClassificatorType")
  private Integer usedClassificatorType;

  @JsonProperty("SourcePlatform")
  private SourcePlatformType sourcePlatform;
}
