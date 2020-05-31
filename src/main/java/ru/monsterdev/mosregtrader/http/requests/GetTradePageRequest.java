package ru.monsterdev.mosregtrader.http.requests;

import org.apache.http.client.methods.HttpGet;
import ru.monsterdev.mosregtrader.http.TraderRequest;

public class GetTradePageRequest extends TraderRequest {
    public static final String URL = "https://market.mosreg.ru/Trade/ViewTrade?id=%d";

    private HttpGet request;

    public GetTradePageRequest(Long tradeId) {
        request = new HttpGet(String.format(URL, tradeId));
    }

    @Override
    public RequestType getType() {
        return RequestType.GET;
    }

    @Override
    public HttpGet getGETRequest() {
        return request;
    }
}
