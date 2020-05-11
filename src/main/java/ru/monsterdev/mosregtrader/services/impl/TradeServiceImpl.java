package ru.monsterdev.mosregtrader.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.monsterdev.mosregtrader.domain.Proposal;
import ru.monsterdev.mosregtrader.domain.ProposalProduct;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.domain.TradeProduct;
import ru.monsterdev.mosregtrader.enums.ProposalStatus;
import ru.monsterdev.mosregtrader.enums.TradeStatus;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.http.TraderResponse;
import ru.monsterdev.mosregtrader.http.requests.GetCreateProposalPageRequest;
import ru.monsterdev.mosregtrader.http.requests.GetProposalInfoRequest;
import ru.monsterdev.mosregtrader.http.requests.GetTradeInfoRequest;
import ru.monsterdev.mosregtrader.http.requests.GetUpdateProposalPageRequest;
import ru.monsterdev.mosregtrader.model.dto.ProposalInfoDto;
import ru.monsterdev.mosregtrader.model.dto.ProposalsInfoDto;
import ru.monsterdev.mosregtrader.model.dto.TradeInfoDto;
import ru.monsterdev.mosregtrader.model.dto.TradesInfoDto;
import ru.monsterdev.mosregtrader.repositories.TradeRepository;
import ru.monsterdev.mosregtrader.services.HttpService;
import ru.monsterdev.mosregtrader.services.TradeService;
import ru.monsterdev.mosregtrader.services.UserService;
import ru.monsterdev.mosregtrader.utils.StringUtil;

@Slf4j
@Service
@Qualifier("tradeService")
public class TradeServiceImpl implements TradeService {

  @Autowired
  private UserService userService;

  @Autowired
  private HttpService httpService;

  @Autowired
  private TradeRepository tradeRepository;

  @Override
  public Trade addTrade(Trade trade) {
    return tradeRepository.add(userService.getCurrentUser(), trade);
  }

  @Override
  public void fetchAll() {
    try {
      // получаем список всех закупок пользователя
      List<Trade> trades = tradeRepository.fetchAll(userService.getCurrentUser());
      // обновление базовой информации о закупке
      for (Trade trade : trades) {
        try {
          fetchTradeInfo(trade);
          fetchTradeItems(trade);
          fetchTradeProposalInfo(trade);
          if (trade.getProposal() != null) {
            fetchProposalItems(trade.getProposal());
          }
        } catch (MosregTraderException ex) {
          // TODO
        }
      }
    } catch (Exception ex) {
      log.error("", ex);
      throw ex;
    }
  }

  private void fetchTradeInfo(@NonNull Trade trade) throws MosregTraderException {
    try {
      log.info("Получение информации о закупке {}", trade.getTradeId());
      TraderResponse response = httpService.sendRequest(new GetTradeInfoRequest(trade.getTradeId()));
      if (response.getCode() != HttpStatus.SC_OK) {
        log.error("Failed to get tradeInfo: code {}, message {}", response.getCode(), response.getEntity());
        throw new MosregTraderException(String.format("Ошибка получения информации о закупке %d", trade.getTradeId()));
      }
      ObjectMapper mapper = new ObjectMapper();
      TradesInfoDto trades = mapper.readValue(response.getEntity(), TradesInfoDto.class);
      if (trades.getTotalrecords() == 0) {
        log.error("Trade {} not found", trade.getTradeId());
        throw new MosregTraderException(String.format("Закупка %d не найдена", trade.getTradeId()));
      }
      TradeInfoDto tradeInfo = trades.getTrades().get(0);
      trade.setName(tradeInfo.getTradeName());
      trade.setStatus(TradeStatus.valueOf(tradeInfo.getTradeState()));
      trade.setBeginDT(tradeInfo.getPublicationDate());
      trade.setEndDT(tradeInfo.getFillingApplicationEndDate());
      trade.setNmc(tradeInfo.getInitialPrice());
    } catch (IOException ex) {
      log.error("", ex);
      throw new MosregTraderException(String.format("Ошибка обработки информации о закупке %d", trade.getTradeId()));
    }
  }

  private void fetchTradeItems(@NonNull Trade trade) throws MosregTraderException {
    try {
      log.info("Получение информаци по позициям закупки {}", trade.getTradeId());
      TraderResponse response = httpService.sendRequest(new GetCreateProposalPageRequest(trade.getTradeId()));
      if (response.getCode() != HttpStatus.SC_OK) {
        log.error("Failed to get trade products: code {}, message {}", response.getCode(), response.getEntity());
        throw new MosregTraderException(String.format("Ошибка получения позиций по закупке %d", trade.getTradeId()));
      }
      Set<TradeProduct> products = StringUtil.parseTradeForProducts(response.getEntity());
      if (products.isEmpty()) {
        log.error("Trade products list is empty");
        throw new MosregTraderException(String.format("Ошибка: по закупке %d не найдено ни одной позиции", trade.getTradeId()));
      }
      trade.setTradeProducts(products);
    } catch (Exception ex) {
      log.error("", ex);
      throw new MosregTraderException(String.format("Ошибка обработки информации о закупке %d", trade.getTradeId()));
    }
  }

  private void fetchTradeProposalInfo(@NonNull Trade trade) throws MosregTraderException {
    try {
      log.info("Получение информации о поданном предложении на закупку {}", trade.getTradeId());
      TraderResponse response = httpService.sendRequest(new GetProposalInfoRequest(trade.getTradeId()));
      if (response.getCode() != HttpStatus.SC_OK) {
        log.error("Failed to get proposal id, code {}, message {}", response.getCode(), response.getEntity());
        throw new MosregTraderException(String.format("Ошибка получения информации о предложении на закупку %d",
            trade.getTradeId()));
      }
      ObjectMapper mapper = new ObjectMapper();
      ProposalsInfoDto proposals = mapper.readValue(response.getEntity(), ProposalsInfoDto.class);
      // предложение уже было подано, получаем информации о торговых позициях
      if (proposals.getTotalrecords() >= 1) {
        ProposalInfoDto proposalInfo = proposals.getProposals().get(0);
        Proposal proposal = new Proposal();
        proposal.setId(proposalInfo.getProposalId());
        proposal.setStatus(ProposalStatus.valueOf(proposalInfo.getStatus()));
        trade.setProposal(proposal);
      }
    } catch (IOException ex) {
      log.error("", ex);
      throw new MosregTraderException(String.format("Ошибка обработки информации о предложении %d", trade.getTradeId()));
    }
  }

  private void fetchProposalItems(@NonNull Proposal proposal) throws MosregTraderException {
    try {
      log.info("Получение информаци по позициям предложения {}", proposal.getId());
      TraderResponse response = httpService.sendRequest(new GetUpdateProposalPageRequest(proposal.getId()));
      if (response.getCode() != HttpStatus.SC_OK) {
        log.error("Failed to get proposal products: code {}, message {}", response.getCode(), response.getEntity());
        throw new MosregTraderException(String.format("Ошибка получения позиций по предложению %d", proposal.getId()));
      }
      Set<ProposalProduct> products = StringUtil.parseProposalForProducts(response.getEntity());
      if (products.isEmpty()) {
        log.error("Proposal products list is empty");
        throw new MosregTraderException(String.format("Ошибка: по предложению %d не найдено ни одной позиции", proposal.getId()));
      }
      proposal.setProducts(products);
    } catch (Exception ex) {
      log.error("", ex);
      throw new MosregTraderException(String.format("Ошибка обработки информации о предложении %d", proposal.getId()));
    }
  }


}
