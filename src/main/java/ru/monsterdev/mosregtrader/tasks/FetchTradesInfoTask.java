package ru.monsterdev.mosregtrader.tasks;

import java.util.List;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.services.TradeService;

@Slf4j
@Component
public class FetchTradesInfoTask extends Task<List<Trade>> implements TraderTask {

  @Autowired
  private TradeService tradeService;

  @Override
  protected List<Trade> call() throws Exception {
    log.info("Fetching trades info...");
    List<Trade> trades = tradeService.findAll();
    log.info("Found {} trades: {}", trades.size(), trades);
    long workDone = 0;
    for (Trade trade : trades) {
      updateMessage(trade.getTradeId().toString());
      tradeService.fetchTrade(trade);
      updateProgress(++workDone, trades.size());
    }
    log.info("All trades fetch successfully");
    return trades;
  }

  @Override
  public void start() {
    Thread thread = new Thread(this);
    thread.start();
  }
}
