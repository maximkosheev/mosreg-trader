package ru.monsterdev.mosregtrader.services;

import ru.monsterdev.mosregtrader.http.TraderRequest;
import ru.monsterdev.mosregtrader.http.TraderResponse;

public interface HttpService {
    /**
     * Формирование и отправка HTTP запроса на сайт торговой плащадки
     * @param request - запрос
     * @return полученный ответ
     */
    TraderResponse sendRequest(TraderRequest request);
}
