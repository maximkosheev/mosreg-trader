package ru.monsterdev.mosregtrader.model;

import java.time.LocalDate;
import lombok.Data;
import ru.monsterdev.mosregtrader.enums.TradeStatus;

@Data
public class TradeFilter {
  private Long tradeNum;
  private String tradeName;
  private LocalDate beginFrom;
  private LocalDate beginTo;
  private LocalDate finishFrom;
  private LocalDate finishTo;
  private TradeStatus status;

  public void clear() {
    tradeNum = null;
    tradeName = null;
    beginFrom = null;
    beginTo = null;
    finishFrom = null;
    finishTo = null;
    status = null;
  }
}
