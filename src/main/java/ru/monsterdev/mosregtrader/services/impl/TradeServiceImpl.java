package ru.monsterdev.mosregtrader.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.domain.User;
import ru.monsterdev.mosregtrader.repositories.TradeRepository;
import ru.monsterdev.mosregtrader.services.TradeService;

@Service
@Qualifier("tradeService")
public class TradeServiceImpl implements TradeService {

  @Autowired
  private TradeRepository tradeRepository;

  @Override
  public Trade addTrade(User user, Trade trade) {
    user.getTrades().add(trade);
    trade.setUser(user);
    return tradeRepository.save(trade);
  }
}
