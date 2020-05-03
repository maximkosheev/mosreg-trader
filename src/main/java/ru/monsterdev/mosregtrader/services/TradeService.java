package ru.monsterdev.mosregtrader.services;

import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.domain.User;

public interface TradeService {

  /**
   * Добавить новую закупку trade к списку закупок пользователя user
   * @param user пользователь
   * @param trade закупка
   * @return закупка, сохраненная в базе
   */
  Trade addTrade(User user, Trade trade);
}
