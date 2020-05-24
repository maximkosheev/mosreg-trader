package ru.monsterdev.mosregtrader.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import ru.monsterdev.mosregtrader.enums.ProposalStatus;
import ru.monsterdev.mosregtrader.model.dto.ProductDto;

/**
 * Proposal class
 */
@Data
public class Proposal {

  /**
   * Идентификатор предложения на площадке
   */
  private Long id;

  /**
   * Статус данного предложения
   */
  private ProposalStatus status = ProposalStatus.INITIAL;

  /**
   * Торговые позиции по данному предложению
   */
  private Set<ProductDto> products = new HashSet<>();
}
