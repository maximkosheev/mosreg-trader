package ru.monsterdev.mosregtrader.algorithms;

import java.math.BigDecimal;
import java.util.function.BiFunction;
import ru.monsterdev.mosregtrader.domain.Trade;

/**
 * Вычисление требуемой цены
 */
public interface CalcRequiredPriceAlgorithm extends BiFunction<Trade, BigDecimal, BigDecimal> {

}
