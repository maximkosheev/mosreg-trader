package ru.monsterdev.mosregtrader.services;

import java.util.List;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.domain.User;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;

public interface TradeService {

  /**
   * Возвращает список всех сохраненных закупок пользователя
   * @return список закупок пользователя
   */
  List<Trade> findAll();
  /**
   * Добавить новую закупку trade к списку закупок пользователя user
   * @param trade закупка
   * @return закупка, сохраненная в базе
   * @throws MosregTraderException если возникли какие-то проблемы с скачиванием информации
   */
  Trade addTrade(Trade trade) throws MosregTraderException;


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
  void submitProposals();
}
