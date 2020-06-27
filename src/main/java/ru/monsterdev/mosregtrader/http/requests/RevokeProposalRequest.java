package ru.monsterdev.mosregtrader.http.requests;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import ru.monsterdev.mosregtrader.domain.Proposal;
import ru.monsterdev.mosregtrader.http.Session;
import ru.monsterdev.mosregtrader.http.TraderRequest;

public class RevokeProposalRequest extends TraderRequest {
  private static final String URL = "https://api.market.mosreg.ru/api/Applications/%d/Revoke";
  private static final String REFERER_URL = "https://market.mosreg.ru/Application/ViewApplication/%d";

  private HttpPost request;

  public RevokeProposalRequest(Proposal proposal) {
    request = new HttpPost(String.format(URL, proposal.getId()));
    request.setHeader("User-Agent", USER_AGENT);
    request.setHeader(HTTP.CONTENT_TYPE, "application/json");
    request.setHeader("Host", "api.market.mosreg.ru");
    request.setHeader("Origin", "https://market.mosreg.ru");
    request.setHeader("Referer", String.format(REFERER_URL, proposal.getId()));
    request.setHeader("XXX-TenantId-Header", "2");
    request.setHeader("Authorization", Session.getInstance().getProperty("authCode"));
    StringEntity se = new StringEntity("", ContentType.APPLICATION_JSON);
    request.setEntity(se);
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
