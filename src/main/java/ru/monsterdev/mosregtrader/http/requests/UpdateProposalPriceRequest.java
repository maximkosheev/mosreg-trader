package ru.monsterdev.mosregtrader.http.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.http.Session;
import ru.monsterdev.mosregtrader.http.TraderRequest;
import ru.monsterdev.mosregtrader.model.dto.ProductDto;
import ru.monsterdev.mosregtrader.model.dto.ProposalEditPriceDto;

public class UpdateProposalPriceRequest extends TraderRequest {
    private static final String URL = "https://api.market.mosreg.ru/api/Application/EditPublished";
    private static final String REFERER_URL = "https://market.mosreg.ru/Application/EditPrice?applicationId=%d";

    private HttpPost request;

    public UpdateProposalPriceRequest(Trade trade) {
        request = new HttpPost(URL);
        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader(HTTP.CONTENT_TYPE, "application/json");
        request.setHeader("Host", "api.market.mosreg.ru");
        request.setHeader("Origin", "https://market.mosreg.ru");
        request.setHeader("Referer", String.format(REFERER_URL, trade.getProposal().getId()));
        request.setHeader("XXX-TenantId-Header", "2");
        request.setHeader("Authorization", Session.getInstance().getProperty("authCode"));
        ObjectMapper mapper = new ObjectMapper();
        ProposalEditPriceDto proposalEditPriceDto = new ProposalEditPriceDto();
        proposalEditPriceDto.setId(trade.getProposal().getId());
        proposalEditPriceDto.setTradeId(trade.getTradeId());
        proposalEditPriceDto.setTradeName(trade.getName());
        proposalEditPriceDto.setFillEndDate(trade.getEndDT());
        proposalEditPriceDto.setInitialPrice(trade.getNmc());
        proposalEditPriceDto.setPrice(trade.getProposal().getProducts().stream()
                .map(ProductDto::getSumm)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        proposalEditPriceDto.setProducts(trade.getProposal().getProducts().stream().map(proposalProduct -> {
            ProductDto product = new ProductDto();
            product.setId(proposalProduct.getId());
            product.setPrice(proposalProduct.getPrice());
            //TODO задать значение всех полей
            return product;
        }).collect(Collectors.toSet()));
        try {
            String requestBody = mapper.writeValueAsString(proposalEditPriceDto);
            StringEntity se = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
            request.setEntity(se);
        } catch (Exception ex) {
            request.setEntity(null);
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
