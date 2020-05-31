package ru.monsterdev.mosregtrader.services.scheduled;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.enums.TradeStatus;
import ru.monsterdev.mosregtrader.services.TradeService;

@Slf4j
@Component
@AllArgsConstructor
public class UpdateTradesInfoService implements Runnable {

  private final TradeService tradeService;

  @Override
  public void run() {
    try {
      log.info("Time to update trades");
      List<Trade> trades = tradeService.findAll().stream()
          .filter(trade -> trade.getFilterStatus() != TradeStatus.ARCHIVED
              && trade.getFilterStatus() != TradeStatus.CLOSED)
          .collect(Collectors.toList());
      log.info("It was found {} trades for update: {}", trades.size(), trades);
      tradeService.updateTrades(trades);
      log.info("Trades updated successfully");
    } catch (Exception ex) {
      log.error("Trades updated with error");
    }
  }
}