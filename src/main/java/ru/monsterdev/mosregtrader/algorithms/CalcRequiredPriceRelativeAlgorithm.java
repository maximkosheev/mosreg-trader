package ru.monsterdev.mosregtrader.algorithms;

import java.math.BigDecimal;
import ru.monsterdev.mosregtrader.domain.Trade;

public class CalcRequiredPriceRelativeAlgorithm implements CalcRequiredPriceAlgorithm {

  @Override
  public BigDecimal apply(Trade trade, BigDecimal currentPrice) {
    return currentPrice.subtract(currentPrice.multiply(trade.getRelativeReduceValue()));
  }
}
