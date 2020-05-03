package ru.monsterdev.mosregtrader.services.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.monsterdev.mosregtrader.domain.FilterOption;
import ru.monsterdev.mosregtrader.enums.FilterType;
import ru.monsterdev.mosregtrader.repositories.FilterRepository;
import ru.monsterdev.mosregtrader.services.DictionaryService;

@Service
@Qualifier("dictionaryService")
public class DictionaryServiceImpl implements DictionaryService {

  @Autowired
  private FilterRepository filterRepository;

  @Override
  public FilterOption saveFilter(FilterType type, FilterOption filter) {
    filter.setType(type);
    return filterRepository.save(filter);
  }

  @Override
  public List<FilterOption> findAllFilters(FilterType type) {
    return filterRepository.findByType(type);
  }
}
