package ru.monsterdev.mosregtrader.algorithms;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.enums.ReducePriceType;

@Component
public class CalcRequiredPriceDispatcher implements BiFunction<Trade, BigDecimal, BigDecimal> {

  // список алгоритмов для вычисления требуемой цены по закупке
  private Map<ReducePriceType, CalcRequiredPriceAlgorithm> calcRequiredPriceAlgorithms;

  public CalcRequiredPriceDispatcher() {
    calcRequiredPriceAlgorithms = new HashMap<>();
    calcRequiredPriceAlgorithms.put(ReducePriceType.ABSOLUTE, new CalcRequiredPriceAbsoluteAlgorithm());
    calcRequiredPriceAlgorithms.put(ReducePriceType.RELATIVE, new CalcRequiredPriceRelativeAlgorithm());
  }

  @Override
  public BigDecimal apply(Trade trade, BigDecimal currentPrice) {
    return calcRequiredPriceAlgorithms.get(trade.getReduceType()).apply(trade, currentPrice);
  }
}
