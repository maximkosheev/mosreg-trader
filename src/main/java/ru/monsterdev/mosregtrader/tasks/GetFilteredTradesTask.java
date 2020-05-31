package ru.monsterdev.mosregtrader.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.http.TraderResponse;
import ru.monsterdev.mosregtrader.http.requests.GetTradeListRequest;
import ru.monsterdev.mosregtrader.model.dto.TradeFilterDto;
import ru.monsterdev.mosregtrader.model.dto.TradesInfoDto;
import ru.monsterdev.mosregtrader.services.HttpService;

/**
 * Класс, описывающий задачу получения информации о закупках, удовлетворяющих фильтру, который задал пользователь
 *
 * @author madmax
 */
@Component
@Scope("prototype")
public class GetFilteredTradesTask extends Task<TradesInfoDto> implements TraderTask {

  private static final String GET_FILTERED_TRADES_1 = "Ошибка при получении списка закупок";

  @Autowired
  private HttpService httpService;

  private TradeFilterDto filter;

  public GetFilteredTradesTask() {
    filter = new TradeFilterDto();
  }

  public GetFilteredTradesTask(TradeFilterDto filter) {
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

  @Override
  public void start() {
    Thread thread = new Thread(this);
    thread.start();
  }
}
