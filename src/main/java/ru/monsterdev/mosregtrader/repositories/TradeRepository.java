package ru.monsterdev.mosregtrader.repositories;

import java.util.List;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.domain.User;

public interface TradeRepository {

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

  /**
   * Удаляет закупку с идентификатором tradeId из списка закупок пользователя user
   * @param user пользователь
   * @param tradeId идентификатор закупки
   */
  void delete(User user, long tradeId);

  /**
   * Обновление информации о закупке в локальной базе
   * @param trade закупка
   */
  void save(Trade trade);
}
