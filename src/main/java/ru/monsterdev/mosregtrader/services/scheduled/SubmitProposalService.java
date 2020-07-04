package ru.monsterdev.mosregtrader.services.scheduled;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class SubmitProposalService implements Runnable {

  private final TradeService tradeService;

  @Override
  public void run() {
    try {
      log.info("Time to submit proposals");
      if (!LicenseUtil.checkDateLimit(LocalDate.now())) {
        throw new MosregTraderException("Закончился срок действия лицензии");
      }
      // выбираем все те закупки по которым еще не подано предложение и подошло время
      List<Trade> trades = tradeService.findAll().stream()
          .filter(trade -> {
            long duration = Duration.between(LocalDateTime.now(), trade.getEndDT()).toMillis();
            return (duration > 0 && duration <= trade.getActivateTime()) &&
                (trade.getProposal() == null) &&
                (trade.getStatus() == TradeStatus.SUGGESTIONS);
          })
          .collect(Collectors.toList());
      log.info("It was found {} trades for make proposals: {}", trades.size(), trades);
      tradeService.submitProposals(trades);
      log.info("Proposals submitted successfully");
    } catch (Exception ex) {
      log.error("Proposals submitted with error", ex);
    }
  }
}
