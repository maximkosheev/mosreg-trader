package ru.monsterdev.mosregtrader.http.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import ru.monsterdev.mosregtrader.http.Session;
import ru.monsterdev.mosregtrader.http.TraderRequest;
import ru.monsterdev.mosregtrader.model.dto.ProposalFilter;

@Slf4j
public class GetProposalInfoRequest extends TraderRequest {

  private static final String URL = "https://api.market.mosreg.ru/api/ParticipantApplications";

  private HttpPost request;

  public GetProposalInfoRequest(Long tradeId) {
    try {
      ProposalFilter filter = new ProposalFilter();
      filter.setPage(1);
      filter.setItemsPerPage(10);
      filter.setTradeNumber(tradeId);
      ObjectMapper mapper = new ObjectMapper();
      String body = mapper.writeValueAsString(filter);
      request = new HttpPost(URL);
      request.setHeader("User-Agent", USER_AGENT);
      request.setHeader(HTTP.CONTENT_TYPE, "application/json");
      request.setHeader("Host", "api.market.mosreg.ru");
      request.setHeader("Origin", "https://market.mosreg.ru");
      request.setHeader("Referer", "https://market.mosreg.ru/");
      request.setHeader("XXX-TenantId-Header", "2");
      request.setHeader("Authorization", Session.getInstance().getProperty("authCode"));
      StringEntity se = new StringEntity(body, ContentType.APPLICATION_JSON);
      request.setEntity(se);
    } catch (Exception ex) {
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
