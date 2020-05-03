package ru.monsterdev.mosregtrader.config;

import java.io.IOException;
import java.io.InputStream;
import javafx.fxml.FXMLLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.monsterdev.mosregtrader.ui.CertificateListController;
import ru.monsterdev.mosregtrader.ui.LoginController;
import ru.monsterdev.mosregtrader.ui.ProposalController;
import ru.monsterdev.mosregtrader.ui.RegisterController;
import ru.monsterdev.mosregtrader.ui.TradeFilterController;
import ru.monsterdev.mosregtrader.ui.UIComponent;
import ru.monsterdev.mosregtrader.ui.MainController;

@Slf4j
@Configuration
public class ControllersConfig {

  private UIComponent loadComponent(String url) {
    try (InputStream inputStream = getClass().getResourceAsStream(url)) {
      FXMLLoader loader = new FXMLLoader();
      loader.load(inputStream);
      return new UIComponent(loader.getRoot(), loader.getController());
    } catch (IOException ex) {
      log.error("Не могу загрузить ресурс {}: ошибка: ", url, ex);
      return null;
    }
  }

  @Bean(name = "mainView")
  public UIComponent getMainView() {
    return loadComponent("/ru/monsterdev/mosregtrader/ui/main.fxml");
  }

  @Bean(name = "loginView")
  public UIComponent getLoginView() {
    return loadComponent("/ru/monsterdev/mosregtrader/ui/login.fxml");
  }

  @Bean(name = "registerView")
  public UIComponent getRegisterView() {
    return loadComponent("/ru/monsterdev/mosregtrader/ui/register.fxml");
  }

  @Bean(name = "certificateListView")
  public UIComponent getCertificateListView() {
    return loadComponent("/ru/monsterdev/mosregtrader/ui/certificates.fxml");
  }

  @Bean(name = "proposalDataView")
  public UIComponent getProposalDataView() {
    return loadComponent("/ru/monsterdev/mosregtrader/ui/proposal_data.fxml");
  }

  @Bean(name = "tradeFilterView")
  public UIComponent getTradeFilterView() {
    return loadComponent("/ru/monsterdev/mosregtrader/ui/trades_filter.fxml");
  }

  @Bean
  public MainController getMainController() {
    return (MainController)getMainView().getController();
  }

  @Bean
  public LoginController getLoginController() {
    return (LoginController)getLoginView().getController();
  }

  @Bean
  public RegisterController getRegisterController() {
    return (RegisterController)getRegisterView().getController();
  }

  @Bean
  public CertificateListController getCertificateListController() {
    return (CertificateListController)getCertificateListView().getController();
  }

  @Bean
  public TradeFilterController getTradeFilterController() {
    return (TradeFilterController)getTradeFilterView().getController();
  }

  @Bean
  public ProposalController getProposalController() {
    return (ProposalController)getProposalDataView().getController();
  }
}
