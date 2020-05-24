package ru.monsterdev.mosregtrader.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.monsterdev.mosregtrader.algorithms.CalcPriceAlgorithm;
import ru.monsterdev.mosregtrader.domain.Proposal;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.enums.ProposalStatus;
import ru.monsterdev.mosregtrader.enums.TradeStatus;
import ru.monsterdev.mosregtrader.enums.VatRateState;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.http.TraderResponse;
import ru.monsterdev.mosregtrader.http.requests.GetCreateProposalPageRequest;
import ru.monsterdev.mosregtrader.http.requests.GetProposalInfoRequest;
import ru.monsterdev.mosregtrader.http.requests.GetTradeInfoRequest;
import ru.monsterdev.mosregtrader.http.requests.GetUpdateProposalPageRequest;
import ru.monsterdev.mosregtrader.http.requests.PublishProposalRequest;
import ru.monsterdev.mosregtrader.model.dto.ProductDto;
import ru.monsterdev.mosregtrader.model.dto.ProposalInfoDto;
import ru.monsterdev.mosregtrader.model.dto.ProposalsInfoDto;
import ru.monsterdev.mosregtrader.model.dto.PublishProposalDto;
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

  @Autowired
  private CalcPriceAlgorithm calcPriceAlgorithm;

  @Override
  public List<Trade> findAll() {
    return tradeRepository.findAll(userService.getCurrentUser());
  }

  @Override
  public Trade addTrade(Trade trade) throws MosregTraderException {
    return tradeRepository.add(userService.getCurrentUser(), fetchTrade(trade));
  }

  @Override
  public Trade fetchTrade(@NonNull Trade trade) throws MosregTraderException {
    try {
      log.info("Fetching trade {} info", trade.getTradeId());
      TradeInfoDto tradeInfo = fetchTradeInfo(trade);
      trade.setName(tradeInfo.getTradeName());
      trade.setStatus(TradeStatus.valueOf(tradeInfo.getTradeState()));
      trade.setBeginDT(tradeInfo.getPublicationDate());
      trade.setEndDT(tradeInfo.getFillingApplicationEndDate());
      trade.setNmc(tradeInfo.getInitialPrice());
      trade.setProducts(fetchTradeItems(trade));
      ProposalInfoDto proposalInfo = fetchTradeProposalInfo(trade);
      if (proposalInfo != null) {
        Proposal proposal = new Proposal();
        proposal.setId(proposalInfo.getProposalId());
        proposal.setStatus(ProposalStatus.valueOf(proposalInfo.getStatus()));
        proposal.setProducts(fetchProposalItems(proposal));
        trade.setProposal(proposal);
      }
      return trade;
    } catch (Exception ex) {
      log.error("", ex);
      throw new MosregTraderException(ex.getMessage());
    }
  }

  /**
   * Подает предложения по закупкам
   */
  public void submitProposals() {
    try {
      log.info("Time to submit proposals");
      // выбираем все те закупки по которым еще не подано предложение и подошло время
      List<Trade> trades = tradeRepository.findAll(userService.getCurrentUser()).stream()
          .filter(trade -> {
            long duration = Duration.between(LocalDateTime.now(), trade.getEndDT()).toMillis();
            return (duration > 0 && duration <= trade.getActivateTime()) &&
                (trade.getProposal() == null) &&
                (trade.getStatus() == TradeStatus.SUGGESTIONS);
          })
          .collect(Collectors.toList());
      log.info("It was found {} trades for make proposals: {}", trades.size(), trades);
      for (Trade trade : trades) {
        Proposal proposal = submitProposal(trade);
        if (proposal != null) {
          proposal.setProducts(fetchProposalItems(proposal));
          trade.setProposal(proposal);
        }
      }
    } catch (Exception ex) {
      log.error("", ex);
    }
  }

  private TradeInfoDto fetchTradeInfo(@NonNull Trade trade) throws MosregTraderException {
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
      return trades.getTrades().get(0);
    } catch (IOException ex) {
      log.error("", ex);
      throw new MosregTraderException(String.format("Ошибка обработки информации о закупке %d", trade.getTradeId()));
    }
  }

  private Set<ProductDto> fetchTradeItems(@NonNull Trade trade) throws MosregTraderException {
    try {
      log.info("Получение информаци по позициям закупки {}", trade.getTradeId());
      TraderResponse response = httpService.sendRequest(new GetCreateProposalPageRequest(trade.getTradeId()));
      if (response.getCode() != HttpStatus.SC_OK) {
        log.error("Failed to get trade products: code {}, message {}", response.getCode(), response.getEntity());
        throw new MosregTraderException(String.format("Ошибка получения позиций по закупке %d", trade.getTradeId()));
      }
      Set<ProductDto> products = StringUtil.parseTradeForProducts(response.getEntity());
      if (products.isEmpty()) {
        log.error("Trade products list is empty");
        throw new MosregTraderException(String.format("Ошибка: по закупке %d не найдено ни одной позиции", trade.getTradeId()));
      }
      return products;
    } catch (Exception ex) {
      log.error("", ex);
      throw new MosregTraderException(String.format("Ошибка обработки информации о закупке %d", trade.getTradeId()));
    }
  }

  private ProposalInfoDto fetchTradeProposalInfo(@NonNull Trade trade) throws MosregTraderException {
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
      return proposals.getTotalrecords() >= 1 ? proposals.getProposals().get(0) : null;
    } catch (IOException ex) {
      log.error("", ex);
      throw new MosregTraderException(String.format("Ошибка обработки информации о предложении %d", trade.getTradeId()));
    }
  }

  private Set<ProductDto> fetchProposalItems(@NonNull Proposal proposal) throws MosregTraderException {
    try {
      log.info("Получение информаци по позициям предложения {}", proposal.getId());
      TraderResponse response = httpService.sendRequest(new GetUpdateProposalPageRequest(proposal.getId()));
      if (response.getCode() != HttpStatus.SC_OK) {
        log.error("Failed to get proposal products: code {}, message {}", response.getCode(), response.getEntity());
        throw new MosregTraderException(String.format("Ошибка получения позиций по предложению %d", proposal.getId()));
      }
      Set<ProductDto> products = StringUtil.parseProposalForProducts(response.getEntity());
      if (products.isEmpty()) {
        log.error("Proposal products list is empty");
        throw new MosregTraderException(String.format("Ошибка: по предложению %d не найдено ни одной позиции", proposal.getId()));
      }
      return products;
    } catch (Exception ex) {
      log.error("", ex);
      throw new MosregTraderException(String.format("Ошибка обработки информации о предложении %d", proposal.getId()));
    }
  }

  private Proposal submitProposal(@NonNull Trade trade) throws MosregTraderException {
    try {
      log.info("Подача предложения по закупке {}", trade.getTradeId());
      Set<ProductDto> products = trade.getProducts();
      if (products.isEmpty()) {
        log.error("Trade products is empty");
        throw new MosregTraderException(String.format("Ошибка: по закупке %d не надено ни одной позиции", trade.getTradeId()));
      }
      calcPriceAlgorithm.doCalc(products, trade.getStartPrice());
      // получаем общую стоимость закупки (в закупке может быть несколько позиций для каждой из которых задана стоимость)
      BigDecimal totalPrice = products.stream().map(ProductDto::getSumm).reduce(BigDecimal.ZERO, BigDecimal::add);
      PublishProposalDto publishProposalInfo = new PublishProposalDto();
      publishProposalInfo.setContactInfo(trade.getUser().getContactInfo());
      publishProposalInfo.setPrice(totalPrice.setScale(2, RoundingMode.DOWN).toString().replace('.', ','));
      if (trade.getUser().isUseNDS()) {
        publishProposalInfo.setIncludeVatRate(true);
        publishProposalInfo.setIncludeVatRateChecked(true);
        publishProposalInfo.setVatRateState(VatRateState.USE_NDS);
        publishProposalInfo.setVatRate(trade.getUser().getNDS());
      } else {
        publishProposalInfo.setIncludeVatRate(false);
        publishProposalInfo.setIncludeVatRateChecked(false);
        publishProposalInfo.setVatRateState(VatRateState.NOT_USE_NSD);
      }
      publishProposalInfo.setProducts(products);
      String requestBody = new ObjectMapper().writeValueAsString(publishProposalInfo);
      TraderResponse response = httpService.sendRequest(new PublishProposalRequest(trade, requestBody));
      if (response.getCode() != HttpStatus.SC_OK) {
        log.error("Failed to create proposal products: code {}, message {}", response.getCode(), response.getEntity());
        throw new MosregTraderException(String.format("Ошибка при подачи предложения по закупке %d", trade.getTradeId()));
      }
      Proposal proposal = new Proposal();
      proposal.setId(Long.parseLong(response.getEntity()));
      return proposal;
    } catch (Exception ex) {
      log.error("", ex);
      throw new MosregTraderException(String.format("Ошибка подачи предложения по закупке %d", trade.getTradeId()));
    }
  }
}
