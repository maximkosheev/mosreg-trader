package ru.monsterdev.mosregtrader.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.monsterdev.mosregtrader.domain.FilterOption;
import ru.monsterdev.mosregtrader.enums.FilterType;

public interface FilterRepository extends JpaRepository<FilterOption, Integer> {

  List<FilterOption> findByType(FilterType type);
}
