package ru.monsterdev.mosregtrader.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalsInfoDto {
  private Integer totalpages;
  private Integer currpage;
  private Integer totalrecords;
  @JsonProperty("invdata")
  private List<ProposalInfoDto> proposals;
}
