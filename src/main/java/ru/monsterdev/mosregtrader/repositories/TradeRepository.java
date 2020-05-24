package ru.monsterdev.mosregtrader.repositories;

import java.util.List;
import ru.monsterdev.mosregtrader.core.IObservable;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.domain.User;

public interface TradeRepository extends IObservable {

  /**
   * Возвращает список всех закупок пользователя user
   * @param user пользователь
   * @return список закупок пользователя
   */
  List<Trade> findAll(User user);

  /**
   * Добавляет новую закупку к списку закупок текущего пользователя
   * @param user пользователь
   * @param trade закупка
   * @return закупка, сохраненная в БД
   */
  Trade add(User user, Trade trade);
}
