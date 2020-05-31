package ru.monsterdev.mosregtrader.ui.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.enums.ProposalStatus;
import ru.monsterdev.mosregtrader.enums.TradeStatus;
import ru.monsterdev.mosregtrader.model.dto.ProductDto;

/**
 * Класс ProposalViewItem - класс-proxy, служит для отображении информации о предложении пользователя в главном окне
 * программы
 */
public class ProposalViewItem {
  private Boolean selected;
  private Trade trade;

  public ProposalViewItem(Trade trade) {
    selected = false;
    this.trade = trade;
  }

  public Boolean getSelected() {
    return selected;
  }

  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

  public String getTradeId() {
    return trade.getTradeId().toString();
  }

  public String getProposalId() {
    if (trade.getProposal() == null) {
      return "Неопределено";
    }
    return trade.getProposal().getId().toString();
  }

  public String getTradeName() {
    return trade.getName();
  }

  public String getStartTradesDT() {
    DateTimeFormatter dFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    return trade.getBeginDT().format(dFormatter);
  }

  public String getFinishTradesDT() {
    DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    return trade.getEndDT().format(dtFormatter);
  }

  public String getNmc() {
    return trade.getNmc().toString();
  }

  public String getLimit() {
    return trade.getMinTradeVal().toString();
  }

  public ProposalStatus getStatus() {
    if (trade.getProposal() == null) {
      return ProposalStatus.UNDEFINED;
    }
    return trade.getProposal().getStatus();
  }

  public String getProposalsCount() {
    if (trade.getProposalsCount() == null) {
      return "Неопределено";
    }
    return trade.getProposalsCount().toString();
  }

  public String getWinPrice() {
    if (trade.getBestPrice() == null) {
      return "Неопределено";
    }
    return trade.getBestPrice().toPlainString();
  }

  public String getCurrentPrice() {
    if (trade.getProposal() == null || trade.getProposal().getProducts().size() == 0) {
      return "Неопределено";
    }
    return trade
        .getProposal().getProducts().stream()
        .map(ProductDto::getSumm)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2)
        .toPlainString();
  }

  /**
   * Возвращает CSS класс для данного предложения в зависимости от статуса.
   * - информация по закупке "прокисла" - серый
   * - закупка активна, но по ней еще не подано предложение - желтый
   * - закупка активная и по ней уже подано предложение - белый.
   * - закупка завершена и по ней не было подано предложение - красный
   * - закупка завершена и проиграна в результате торгов - красный
   * - закупка завершена и выиграна - зеленый
   */
  public String getCSSClass() {
    String css = "trade-default";
    if (trade.getFilterStatus() == TradeStatus.ARCHIVED) {
      return "trade-archived";
    } else if (trade.getFilterStatus() == TradeStatus.NO_PROPOSAL) {
      css = "trade-no-proposal";
    }
    if (trade.isFinished()) {
      if (trade.getBestProposalId() != null && trade.getBestProposalId().equals(trade.getProposal().getId())) {
        css = "trade-win";
      } else {
        css = "trade-lose";
      }
    }
    return css;
  }

}
