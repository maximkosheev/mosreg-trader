package ru.monsterdev.mosregtrader.repositories.extend;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.domain.User;
import ru.monsterdev.mosregtrader.repositories.TradeRepository;
import ru.monsterdev.mosregtrader.repositories.TradeRepositoryBase;

@Slf4j
@Component
public class TradeRepositoryImpl extends Observable implements TradeRepository {

  private List<Trade> trades = new ArrayList<>();

  @Autowired
  private TradeRepositoryBase tradeRepositoryBase;

  @Override
  public Trade add(User user, Trade trade) {
    try {
      trade.setUser(user);
      Trade dbTrade = tradeRepositoryBase.save(trade);
      trades.add(dbTrade);
      notifyObservers(dbTrade);
      return dbTrade;
    } catch (Exception ex) {
      log.error("", ex);
      throw ex;
    }
  }

  @Override
  public List<Trade> fetchAll(User user) {
    return tradeRepositoryBase.findAllByUser(user);
  }
}
