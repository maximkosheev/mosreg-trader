package ru.monsterdev.mosregtrader.services.scheduled;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.enums.TradeStatus;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.services.TradeService;
import ru.monsterdev.mosregtrader.utils.LicenseUtil;

@Slf4j
@Component
@AllArgsConstructor
public class UpdateTradesInfoService implements Runnable {

  private final TradeService tradeService;

  @Override
  public void run() {
    try {
      log.info("Автоматическое обновление информации по закупкам");
      if (!LicenseUtil.checkDateLimit(LocalDate.now())) {
        throw new MosregTraderException("Закончился срок действия лицензии");
      }
      List<Trade> trades = tradeService.findAll().stream()
          .filter(trade -> trade.getFilterStatus() != TradeStatus.ARCHIVED
              && trade.getFilterStatus() != TradeStatus.CLOSED)
          .collect(Collectors.toList());
      log.info("Будет обновлена информация по {} закупкам: {}", trades.size(), trades);
      tradeService.updateTrades(trades);
      log.info("Информация по всем закупкам обновлена успешно");
    } catch (Exception ex) {
      log.error("Trades updated with error", ex);
    }
  }
}