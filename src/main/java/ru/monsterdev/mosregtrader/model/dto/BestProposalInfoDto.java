package ru.monsterdev.mosregtrader.model.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BestProposalInfoDto {
    private Integer totalCount;
    private BigDecimal bestPrice;
    private Long bestProposalId;
}
