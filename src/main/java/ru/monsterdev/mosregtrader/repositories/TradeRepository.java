package ru.monsterdev.mosregtrader.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.monsterdev.mosregtrader.domain.Trade;

public interface TradeRepository extends JpaRepository<Trade, Long> {

}
