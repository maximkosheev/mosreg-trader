package ru.monsterdev.mosregtrader.services.scheduled;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.enums.TradeStatus;
import ru.monsterdev.mosregtrader.services.TradeService;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateProposalsPrice implements Runnable {

  /**
   * время до окончания торгов по закупке, когда нужно начать снижать стоимость
   */
  @Value("${app.remained-limit-time}")
  private long remainedLimit;

  private final TradeService tradeService;

  public void setRemainedLimit(long remainedLimit) {
    this.remainedLimit = remainedLimit;
  }

  @Override
  public void run() {
    try {
      log.info("Time to update proposals price");
      // выбираем все те закупки по которым еще не подано предложение и подошло время
      List<Trade> trades = tradeService.findAll().stream()
          .filter(trade -> {
            long duration = Duration.between(LocalDateTime.now(), trade.getEndDT()).toMillis();
            return (duration > 0 && duration <= remainedLimit) &&
                (trade.getProposal() != null) &&
                (trade.getStatus() == TradeStatus.SUGGESTIONS);
          })
          .collect(Collectors.toList());
      log.info("It was found {} trades for trade: {}", trades.size(), trades);
      tradeService.updateProposalsPrice(trades);
      log.info("Proposals updated successfully");
    } catch (Exception ex) {
      log.error("Proposals updated with error");
    }
  }

}
