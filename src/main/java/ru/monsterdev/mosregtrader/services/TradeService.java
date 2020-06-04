package ru.monsterdev.mosregtrader.services;

import java.util.List;
import lombok.NonNull;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.model.TradeFilter;

public interface TradeService {

  /**
   * Возвращает список всех сохраненных закупок пользователя
   * @return список закупок пользователя
   */
  List<Trade> findAll();

  /**
   * Возвращает список закупок, удовлетворяющих фильтру
   * @param tradeFilter фильтр
   * @return список закупок
   */
  List<Trade> findAll(TradeFilter tradeFilter);

  /**
   * Возвращает закупку по её идентификатору (имеется ввиду идентификатору в приложении, не площадки)
   * @param tradeId идентификатор
   * @return закупка
   */
  Trade getTradeById(long tradeId);

  /**
   * Добавить новую закупку trade к списку закупок пользователя user
   * @param trade закупка
   * @return закупка, сохраненная в базе
   * @throws MosregTraderException если возникли какие-то проблемы с скачиванием информации
   */
  Trade addTrade(Trade trade) throws MosregTraderException;

  /**
   * Удаляет закупку с идентификатором tradeId
   * @param tradeId идентификатор закупки
   */
  void deleteTrade(long tradeId);

  /**
   * Выполняет загрузку скачивание информации о закупке
   * @param trade загрузка
   * @return закупка с заполненной информацией с площадки
   * @throws MosregTraderException если возникли какие-то проблемы с скачиванием информации
   */
  Trade fetchTrade(Trade trade) throws MosregTraderException;

  /**
   * Подает предложение по всем закупкам, по которым пришло время это делать
   */
  void submitProposals(List<Trade> trades) throws MosregTraderException;

  /**
   * Обновление важной информации по закупкам
   */
  void updateTrades(List<Trade> trades) throws MosregTraderException;

  /**
   * Обновление цены предложений по закупкам
   * @param trades закупки
   */
  void updateProposalsPrice(List<Trade> trades) throws MosregTraderException;

  /**
   * Обновление информации о закупке в локальной базе
   * @param trade закупка
   */
  void saveTrade(@NonNull Trade trade);
}
