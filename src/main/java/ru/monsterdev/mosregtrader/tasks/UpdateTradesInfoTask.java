package ru.monsterdev.mosregtrader.tasks;

import java.util.List;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.services.TradeService;

@Slf4j
@Component
@Scope("prototype")
public class UpdateTradesInfoTask extends Task<List<Trade>> implements TraderTask {
  private List<Trade> trades;
  private final TradeService tradeService;

  public UpdateTradesInfoTask(TradeService tradeService) {
    this(tradeService, null);
  }

  public UpdateTradesInfoTask(TradeService tradeService, List<Trade> trades) {
    this.tradeService = tradeService;
    this.trades = trades;
    if (this.trades == null) {
      this.trades = tradeService.findAll();
    }
  }

  @Override
  protected List<Trade> call() throws Exception {
    log.info("Updating trades info...");
    log.info("Found {} trades: {}", trades.size(), trades);
    tradeService.updateTrades(trades);
    log.info("All trades updated successfully");
    return trades;

  }

  @Override
  public void start() {
    Thread thread = new Thread(this);
    thread.start();
  }
}
