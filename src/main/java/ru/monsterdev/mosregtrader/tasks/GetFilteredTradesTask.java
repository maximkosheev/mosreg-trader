package ru.monsterdev.mosregtrader.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.http.TraderResponse;
import ru.monsterdev.mosregtrader.http.requests.GetTradeListRequest;
import ru.monsterdev.mosregtrader.model.dto.TradeFilter;
import ru.monsterdev.mosregtrader.model.dto.TradesInfoDto;
import ru.monsterdev.mosregtrader.services.HttpService;

/**
 * Класс, описывающий задачу получения информации о закупках, удовлетворяющих фильтру, который задал пользователь
 *
 * @author madmax
 */
public class GetFilteredTradesTask extends Task<TradesInfoDto> {

  private static final String GET_FILTERED_TRADES_1 = "Ошибка при получении списка закупок";

  @Autowired
  private HttpService httpService;

  private TradeFilter filter;

  public GetFilteredTradesTask(TradeFilter filter) {
    this.filter = filter;
  }

  @Override
  protected TradesInfoDto call() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    TraderResponse response = httpService.sendRequest(new GetTradeListRequest(mapper.writeValueAsString(filter)));
    if (response.getCode() != HttpStatus.SC_OK) {
      throw new MosregTraderException(GET_FILTERED_TRADES_1);
    }
    return mapper.readValue(response.getEntity(), TradesInfoDto.class);
  }
}
