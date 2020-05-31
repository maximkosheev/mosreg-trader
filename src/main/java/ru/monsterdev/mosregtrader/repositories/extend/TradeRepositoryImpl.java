package ru.monsterdev.mosregtrader.repositories.extend;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.domain.User;
import ru.monsterdev.mosregtrader.repositories.TradeRepository;
import ru.monsterdev.mosregtrader.repositories.TradeRepositoryBase;

@Slf4j
@Component
public class TradeRepositoryImpl implements TradeRepository {

  private CopyOnWriteArrayList<Trade> trades = new CopyOnWriteArrayList<>();
  //private List<Trade> trades = new ArrayList<>();

  @Autowired
  private TradeRepositoryBase tradeRepositoryBase;

  @Override
  public Trade add(User user, Trade trade) {
    try {
      trade.setUser(user);
      Trade dbTrade = tradeRepositoryBase.save(trade);
      trades.add(dbTrade);
      return dbTrade;
    } catch (Exception ex) {
      log.error("", ex);
      throw ex;
    }
  }

  @Override
  public List<Trade> findAll(User user) {
    // загружаем данные из базы только при первом запросе
    if (trades.isEmpty()) {
      trades.addAll(tradeRepositoryBase.findAllByUser(user));
    }
    return trades;
  }
}
