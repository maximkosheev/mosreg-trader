package ru.monsterdev.mosregtrader.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Статус закупки. Статус закупки имеет двойственную природу. С одной стороны это статус, который задан закупке на
 * самой торговой площадке. Этот вид статуса используется для фильтрации закупок с площадки. С другой стороны
 * каждая закупка имеет локальный статус в рамках приложения. Этот статус используется для фильтрации закупок на
 * главном окне приложения. Статусы торговой площадки имеют положительные коды,
 * внутренние статусы приложения - отрицательные
 */
public enum TradeStatus {
  ALL(0, "Все"),
  SUGGESTIONS(15, "Прием предложений"),
  AGREEING(20, "Согласование"),
  CONTRACTING(40, "Заключение договора"),
  CONTRACTED(50, "Договор заключен"),
  WITHOUT_PROPOSALS(25, "Нет предложений"),
  CANCELED(30, "Отменена"),

  ARCHIVED(-10, "В архиве"),
  CLOSED(-20, "Закрыта"),
  NO_PROPOSAL(-30, "Нет заявки"),
  ACTIVE(-40, "Активна");

  private Integer code;
  private String description;

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  TradeStatus(Integer code, String description) {
    this.code = code;
    this.description = description;
  }

  public static TradeStatus valueOf(int code) {
    switch (code) {
      case 15:
        return SUGGESTIONS;
      case 20:
        return AGREEING;
      case 40:
        return CONTRACTING;
      case 50:
        return CONTRACTED;
      case 25:
        return WITHOUT_PROPOSALS;
      case 30:
        return CANCELED;
      case -10:
        return ARCHIVED;
      default:
        return ALL;
    }
  }

  /**
   * Возвразает все значения статусов от торговой площадки
   * @return список статусов
   */
  public static List<TradeStatus> allFromMosreg() {
    return Arrays.stream(values()).filter(status -> status.code >= 0).collect(Collectors.toList());
  }

  /**
   * Возвращает все значения внутренних статутов приложения
   * @return список статусов
   */
  public static List<TradeStatus> allFromLocal() {
    return Arrays.stream(values()).filter(status -> status.code <= 0).collect(Collectors.toList());
  }


  @Override
  public String toString() {
    return description;
  }
}
