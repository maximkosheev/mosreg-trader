package ru.monsterdev.mosregtrader.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ProposalFilter {
  @JsonProperty("page")
  private Integer page;
  @JsonProperty("itemsPerPage")
  private Integer itemsPerPage;
  @JsonProperty("TradeNumber")
  private Long tradeNumber;
  @JsonProperty("State")
  private Integer status;
  @JsonProperty("PublishDateFrom")
  private LocalDate publishDateFrom;
  @JsonProperty("PublishDateTo")
  private LocalDate publishDateTo;
  @JsonProperty("RevokeDateFrom")
  private LocalDate revokeDateFrom;
  @JsonProperty("RevokeDateTo")
  private LocalDate revokeDateTo;
}
