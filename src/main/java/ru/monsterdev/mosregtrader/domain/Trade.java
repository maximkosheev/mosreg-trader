package ru.monsterdev.mosregtrader.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Data;
import ru.monsterdev.mosregtrader.enums.TradeStatus;

/**
 * Класс описывающий сущность "Закупка", хрянящаяся в БД
 *
 * @author madmax
 */
@Data
@Entity
@Table(name = "trades")
public class Trade {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * идентификатор закупки, полученный от площадки
   */
  @Column(name = "trade_id", nullable = false)
  private Long tradeId;

  /**
   * Наименование закупки
   */
  @Transient
  private String name;

  /**
   * Текущий стратус закупки
   */
  @Transient
  private TradeStatus status;

  /**
   * Дата и время начала торгов по закупке
   */
  @Transient
  private LocalDateTime beginDT;

  /**
   * Дата и время окончания торгов
   */
  @Transient
  private LocalDateTime endDT;

  /**
   * Время последнего обновления информации по закупке
   */
  @Transient
  private LocalDateTime updateDT;

  /**
   * Начальная стоимость, заданная заказчиком
   */
  @Transient
  private BigDecimal nmc;

  /**
   * Начальная стоимость, задананя поставщиком.
   * Этот атрибут задается при добавлении закупки в базу, и затем, когда придет время подавать предложение по этой
   * закупке это будет начальной ценой предложения.
   */
  @Transient
  private BigDecimal startPrice;

  /**
   * Количество поданных предложений по данной закупке
   */
  @Transient
  private Integer proposalsCount;

  /**
   * Идентификатор лучшего предложения. Лучшее предложение то, которое имеет наименьшую сумму, и находится первым в
   * списке предложений
   */
  @Transient
  private Long bestProposalId;

  /**
   * Сумма по лучшему предложению
   */
  @Transient
  private BigDecimal bestPrice;

  /**
   * Минимальное пороговое значение стоимости контракта
   */
  @Column(name = "min_val", precision = 11, scale = 2, nullable = false)
  private BigDecimal minTradeVal;

  /**
   * Время в милисекундах до окончания приема предложений когда формируется реальное предложение на площадке
   */
  @Column(name = "activate_time", nullable = false)
  private Long activateTime;

  @Transient
  private Proposal proposal;

  /**
   * Список товаров/позиций по данной закупке
   */
  @Transient
  private Set<TradeProduct> tradeProducts = new HashSet<>();

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Transient
  private Boolean updated = false;
}
