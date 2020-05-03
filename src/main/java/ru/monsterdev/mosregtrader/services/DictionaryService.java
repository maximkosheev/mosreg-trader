package ru.monsterdev.mosregtrader.services;

import java.util.List;
import ru.monsterdev.mosregtrader.domain.FilterOption;
import ru.monsterdev.mosregtrader.enums.FilterType;

public interface DictionaryService {
    /**
     * Сохраняет новый фильтр
     * @param type - тип фильтра
     * @param filter - новый фильтр
     * @return - возвращает сохраненный фильтр
     */
    FilterOption saveFilter(FilterType type, FilterOption filter);

    /**
     * Возвращает все фильтры указанного типа, сохраненные в БД
     * @return список фильтров по предложениям
     */
    List<FilterOption> findAllFilters(FilterType type);
}
