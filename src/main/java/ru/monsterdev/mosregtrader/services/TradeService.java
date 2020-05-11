package ru.monsterdev.mosregtrader.services;

import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.domain.User;

public interface TradeService {

  /**
   * Добавить новую закупку trade к списку закупок пользователя user
   * @param trade закупка
   * @return закупка, сохраненная в базе
   */
  Trade addTrade(Trade trade);


  /**
   * Выполняет загрузку всех закупок из БД во внутреннюю памятьи и обновление информации по каждой из них.
   * Информацию о закупке получаем с площадки
   */
  void fetchAll();

}
