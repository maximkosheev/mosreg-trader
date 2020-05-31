package ru.monsterdev.mosregtrader.http.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import ru.monsterdev.mosregtrader.http.TraderRequest;
import ru.monsterdev.mosregtrader.model.dto.TradeFilterDto;

@Slf4j
public class GetTradeInfoRequest extends TraderRequest {

  private static final String URL = "https://api.market.mosreg.ru/api/Trade/GetTradesForParticipantOrAnonymous";

  private HttpPost request;

  public GetTradeInfoRequest(Long tradeId) {
    super();
    TradeFilterDto filter = new TradeFilterDto();
    filter.setIsImmediate(false);
    filter.setOnlyTradesWithMyApplications(false);
    filter.setShowOnlyOwnTrades(false);
    filter.setPage(1);
    filter.setItemsPerPage(10);
    filter.setId(tradeId);
    request = new HttpPost(URL);
    request.setHeader("User-Agent", USER_AGENT);
    request.setHeader(HTTP.CONTENT_TYPE, "application/json");
    request.setHeader("Host", "api.market.mosreg.ru");
    request.setHeader("Origin", "https://market.mosreg.ru");
    request.setHeader("Referer", "https://market.mosreg.ru/");
    request.setHeader("XXX-TenantId-Header", "2");
    ObjectMapper mapper = new ObjectMapper();
    try {
      StringEntity se = new StringEntity(mapper.writeValueAsString(filter), ContentType.APPLICATION_JSON);
      request.setEntity(se);
    } catch (JsonProcessingException ex) {
      log.error("", ex);
    }
  }

  @Override
  public RequestType getType() {
    return RequestType.POST;
  }

  @Override
  public HttpPost getPOSTRequest() {
    return request;
  }

}
