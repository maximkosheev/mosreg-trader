package ru.monsterdev.mosregtrader.repositories.extend;

import java.util.List;
import java.util.Optional;
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
  public void delete(User user, long tradeId) {
    try {
      Optional<Trade> optTrade = trades.stream().filter(trade1 -> trade1.getTradeId() == tradeId).findFirst();
      optTrade.ifPresent(trade -> {
        tradeRepositoryBase.delete(trade);
        trades.remove(trade);
      });
    } catch (Exception ex) {
      log.error("", ex);
      throw ex;
    }
  }

  @Override
  public void save(Trade trade) {
    try {
      tradeRepositoryBase.save(trade);
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
