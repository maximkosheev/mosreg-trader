package ru.monsterdev.mosregtrader.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
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
import ru.monsterdev.mosregtrader.http.requests.GetTradePageRequest;
import ru.monsterdev.mosregtrader.http.requests.GetUpdateProposalPageRequest;
import ru.monsterdev.mosregtrader.http.requests.PublishProposalRequest;
import ru.monsterdev.mosregtrader.model.SupplierProposal;
import ru.monsterdev.mosregtrader.model.TradeFilter;
import ru.monsterdev.mosregtrader.model.dto.BestProposalInfoDto;
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
  public List<Trade> findAll(TradeFilter tradeFilter) {
    return tradeRepository.findAll(userService.getCurrentUser()).stream()
        .filter(trade -> (tradeFilter.getTradeNum() == null || trade.getTradeId().equals(tradeFilter.getTradeNum()))
            && (tradeFilter.getTradeName() == null || trade.getName().equals(tradeFilter.getTradeName()))
            && (tradeFilter.getBeginFrom() == null || trade.getBeginDT().toLocalDate().isAfter(tradeFilter.getBeginFrom()))
            && (tradeFilter.getBeginTo() == null || trade.getBeginDT().toLocalDate().isBefore(tradeFilter.getBeginTo()))
            && (tradeFilter.getFinishFrom() == null || trade.getEndDT().toLocalDate().isAfter(tradeFilter.getFinishFrom()))
            && (tradeFilter.getFinishTo() == null || trade.getEndDT().toLocalDate().isBefore(tradeFilter.getFinishTo()))
            && (tradeFilter.getStatus() == null || tradeFilter.getStatus() == TradeStatus.ALL || trade.getFilterStatus() == tradeFilter.getStatus()))
        .collect(Collectors.toList());
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
        if (trade.getProposal().getStatus() == ProposalStatus.ACTIVE) {
          BestProposalInfoDto bestProposalInfo = fetchBestProposalInfo(trade);
          if (bestProposalInfo != null) {
            trade.setProposalsCount(bestProposalInfo.getTotalCount());
            trade.setBestProposalId(bestProposalInfo.getBestProposalId());
            trade.setBestPrice(bestProposalInfo.getBestPrice());
          }
        }
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
  @Override
  public void submitProposals(List<Trade> trades) throws MosregTraderException {
    try {
      for (Trade trade : trades) {
        Proposal proposal = submitProposal(trade);
        if (proposal != null) {
          proposal.setProducts(fetchProposalItems(proposal));
          trade.setProposal(proposal);
        }
      }
    } catch (Exception ex) {
      log.error("", ex);
      throw ex;
    }
  }

  @Override
  synchronized public void updateTrades(List<Trade> trades) throws MosregTraderException {
    try {
      for (Trade trade : trades) {
        TradeInfoDto tradeInfo = fetchTradeInfo(trade);
        trade.setName(tradeInfo.getTradeName());
        trade.setStatus(TradeStatus.valueOf(tradeInfo.getTradeState()));
        trade.setBeginDT(tradeInfo.getPublicationDate());
        trade.setEndDT(tradeInfo.getFillingApplicationEndDate());
        trade.setNmc(tradeInfo.getInitialPrice());
        if (trade.getProposal() != null) {
          ProposalInfoDto proposalInfo = fetchTradeProposalInfo(trade);
          assert proposalInfo != null;
          trade.getProposal().setStatus(ProposalStatus.valueOf(proposalInfo.getStatus()));
          if (trade.getProposal().getStatus() == ProposalStatus.ACTIVE) {
            BestProposalInfoDto bestProposalInfo = fetchBestProposalInfo(trade);
            if (bestProposalInfo != null) {
              trade.setProposalsCount(bestProposalInfo.getTotalCount());
              trade.setBestProposalId(bestProposalInfo.getBestProposalId());
              trade.setBestPrice(bestProposalInfo.getBestPrice());
            }
          }
        }
      }
    } catch (Exception ex) {
      log.error("", ex);
      throw ex;
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

  private BestProposalInfoDto fetchBestProposalInfo(@NonNull Trade trade) throws MosregTraderException {
    try {
      log.info("Получение информаци о предложениях по закупке {}", trade.getTradeId());
      TraderResponse response = httpService.sendRequest(new GetTradePageRequest(trade.getTradeId()));
      if (response.getCode() != HttpStatus.SC_OK) {
        log.error("Failed to get trade best proposal: code {}, message {}", response.getCode(), response.getEntity());
        throw new MosregTraderException(String.format("Ошибка получения лучшего предложения по закупке %d", trade.getTradeId()));
      }
      List<SupplierProposal> supplierProposals = StringUtil.parseForProposals(response.getEntity());
      if (supplierProposals.isEmpty()) {
        log.error("Supplier proposal products list is empty");
        throw new MosregTraderException(String.format("Ошибка: по закупке %d не найдено ни одного предложения", trade.getTradeId()));
      }
      supplierProposals.sort(Comparator.comparing(SupplierProposal::getPrice));
      BestProposalInfoDto bestProposalInfoDto = new BestProposalInfoDto();
      bestProposalInfoDto.setTotalCount(supplierProposals.size());
      bestProposalInfoDto.setBestProposalId(supplierProposals.get(0).getId());
      bestProposalInfoDto.setBestPrice(supplierProposals.get(0).getPrice());
      return bestProposalInfoDto;
    } catch (Exception ex) {
      log.error("", ex);
      throw new MosregTraderException(String.format("Ошибка обработки информации о предложениях по закупке %d", trade.getTradeId()));
    }
  }
}
