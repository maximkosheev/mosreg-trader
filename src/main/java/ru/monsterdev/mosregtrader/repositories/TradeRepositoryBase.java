package ru.monsterdev.mosregtrader.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.domain.User;

public interface TradeRepositoryBase extends JpaRepository<Trade, Long> {

  /**
   * Возвращает список всех закупок пользователя
   * @param user пользователь
   * @return список всех закупок
   */
  List<Trade> findAllByUser(User user);
}
