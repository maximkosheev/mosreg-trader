package ru.monsterdev.mosregtrader.model;

import lombok.Data;

import java.math.BigDecimal;
import ru.monsterdev.mosregtrader.enums.ReducePriceType;

@Data
public class ProposalData {

  /**
   * Минимальная цена предложения, до которой поставщик считает возможным опуститься
   */
  private BigDecimal minTradeVal;
  /**
   * Время в мсек до конца приема предложений по закупке, когда необходимо подать предложение
   * Если не задано, то предложение подается сразу
   */
  private long activateTime;

  /**
   * Начальная цена предложения по закупке.
   */
  private BigDecimal startTradeVal;

  /**
   * Вариант снижения цены
   */
  private ReducePriceType reducePriceType;
  /**
   * Снижение на абсолютное значение
   */
  private BigDecimal absoluteReduceValue;
  /**
   * Значение на относительное значение
   */
  private BigDecimal relativeReduceValue;
}
