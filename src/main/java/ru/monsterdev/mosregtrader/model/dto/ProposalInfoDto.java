package ru.monsterdev.mosregtrader.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import lombok.Data;
import ru.monsterdev.mosregtrader.utils.converters.DateDeserializers;
import ru.monsterdev.mosregtrader.utils.converters.DateSerializers;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalInfoDto {
  @JsonProperty("IncomingNumber")
  private Long proposalId;
  @JsonProperty("TradeNumber")
  private Long tradeId;
  @JsonProperty("PublishDate")
  @JsonDeserialize(using = DateDeserializers.LocalDateTimeDeserializer.class)
  @JsonSerialize(using = DateSerializers.LocalDateTimeSerializer.class)
  private LocalDateTime publishDate;
  @JsonProperty("RevokeDate")
  @JsonDeserialize(using = DateDeserializers.LocalDateTimeDeserializer.class)
  @JsonSerialize(using = DateSerializers.LocalDateTimeSerializer.class)
  private LocalDateTime revokeDate;
  @JsonProperty("State")
  private Integer status;
}
