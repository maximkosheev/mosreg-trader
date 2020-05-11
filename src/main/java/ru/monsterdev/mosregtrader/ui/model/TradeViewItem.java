package ru.monsterdev.mosregtrader.ui.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import ru.monsterdev.mosregtrader.model.dto.TradeInfoDto;

/**
 * Класс хранит информацию о закупке, необходимую для работы UI,
 * но которую не требуется хранить в бд
 */
public class TradeViewItem {
    private BooleanProperty selected;
    private TradeInfoDto tradeInfo;

    public TradeViewItem(TradeInfoDto tradeInfo) {
        this.selected = new SimpleBooleanProperty(false);
        this.tradeInfo = tradeInfo;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public boolean isSelected() {
        return selected.get();
    }

    public Long getId() {
        return tradeInfo.getId();
    }

    public void setId(Long id) {
        tradeInfo.setId(id);
    }

    public String getName() {
        return tradeInfo.getTradeName();
    }

    public void setName(String name) {
        tradeInfo.setTradeName(name);
    }

    public String getCustomer() {
        return tradeInfo.getCustomerFullName();
    }

    public void setCustomer(String customer) {
        tradeInfo.setCustomerFullName(customer);
    }

    public BigDecimal getInitialPrice() {
        return tradeInfo.getInitialPrice();
    }

    public void setInitialPrice(BigDecimal initialPrice) {
        tradeInfo.setInitialPrice(initialPrice);
    }

    public LocalDateTime getPublicationDate() {
        return tradeInfo.getPublicationDate();
    }

    public void setPublicationDate(LocalDateTime publicationDate) {
        tradeInfo.setPublicationDate(publicationDate);
    }

    public LocalDateTime getFillingApplicationEndDate() {
        return tradeInfo.getFillingApplicationEndDate();
    }

    public void setFillingApplicationEndDate(LocalDateTime fillingApplicationEndDate) {
        tradeInfo.setFillingApplicationEndDate(fillingApplicationEndDate);
    }

    public int getApplicationsCount() {
        return tradeInfo.getApplicationsCount();
    }

    public void setApplicationsCount(int applicationsCount) {
        tradeInfo.setApplicationsCount(applicationsCount);
    }

    public boolean isImmediate() {
        return tradeInfo.getIsImmediate();
    }

    public void setImmediate(boolean immediate) {
        tradeInfo.setIsImmediate(immediate);
    }

    public boolean isParticipantHasApplicationsOnTrade() {
        return tradeInfo.getParticipantHasApplicationsOnTrade();
    }

    public void setParticipantHasApplicationsOnTrade(boolean participantHasApplicationsOnTrade) {
        tradeInfo.setParticipantHasApplicationsOnTrade(participantHasApplicationsOnTrade);
    }

    public boolean isHasDealSignedOutsideEShop() {
        return tradeInfo.getHasDealSignedOutsideEShop();
    }

    public void setHasDealSignedOutsideEShop(boolean hasDealSignedOutsideEShop) {
        tradeInfo.setHasDealSignedOutsideEShop(hasDealSignedOutsideEShop);
    }

    public Date getLastModificationDate() {
        return tradeInfo.getLastModificationDate();
    }

    public void setLastModificationDate(Date lastModificationDate) {
        tradeInfo.setLastModificationDate(lastModificationDate);
    }

    public int getState() {
        return tradeInfo.getTradeState();
    }

    public void setState(int state) {
        tradeInfo.setTradeState(state);
    }

    public String getStateName() {
        return tradeInfo.getTradeStateName();
    }

    public void setStateName(String stateName) {
        tradeInfo.setTradeStateName(stateName);
    }

    public TradeInfoDto getInfo() {
        return tradeInfo;
    }
}
