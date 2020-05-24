package ru.monsterdev.mosregtrader.tasks;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.services.TradeService;

@Slf4j
@Component
@AllArgsConstructor
public class SubmitProposalTask implements Runnable {

  private final TradeService tradeService;

  @Override
  public void run() {
    tradeService.submitProposals();
  }
}
