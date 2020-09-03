package ru.monsterdev.mosregtrader.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.model.CertificateInfo;
import ru.monsterdev.mosregtrader.model.ProposalData;
import ru.monsterdev.mosregtrader.model.dto.TradeInfoDto;
import ru.monsterdev.mosregtrader.ui.ProfileController.ShowMode;

@Slf4j
@Component
public class UIDispatcher {

  private Stage primaryStage;

  @Autowired
  private UIComponent loginView;

  @Autowired
  private UIComponent profileView;

  @Autowired
  private UIComponent certificateListView;

  @Autowired
  private UIComponent mainView;

  @Autowired
  private UIComponent tradeFilterView;

  @Autowired
  private UIComponent proposalDataView;

  private Map<String, Scene> scenes = new HashMap<>();

  @PostConstruct
  public void init() {
    scenes.put("login", new Scene(loginView.getView(), 600, 400));
    scenes.put("profile", new Scene(profileView.getView(), 600, 700));
    scenes.put("certificates", new Scene(certificateListView.getView(), 450, 355));
    scenes.put("main", new Scene(mainView.getView(), 1024, 768));
    scenes.put("tradeFilter", new Scene(tradeFilterView.getView(), 860, 770));
    scenes.put("proposalData", new Scene(proposalDataView.getView(), 400, 420));
  }

  public Stage getPrimaryStage() {
    return primaryStage;
  }

  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public void close() {
    primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
  }

  public void showLoginUI() {
    primaryStage.setScene(scenes.get("login"));
    primaryStage.setTitle("MosregTrader - Вход");
    loginView.getController().bootstrap();
    primaryStage.show();
  }

  public void showProfileUI(ShowMode showMode) {
    Stage stage = showMode == ShowMode.REGISTER ? primaryStage : new Stage();
    stage.setTitle(showMode == ShowMode.REGISTER ?
        "MosregTrader - Регистрация пользователя" :
        "MosregTrader - Редактирование пользователя");
    stage.setScene(scenes.get("profile"));
    profileView.getController().bootstrap(showMode);
    if (showMode == ShowMode.REGISTER) {
      stage.show();
    } else if (showMode == ShowMode.EDIT) {
      stage.showAndWait();
    }
  }

  public Optional<CertificateInfo> showCertificateListUI() {
    Stage stage = new Stage();
    stage.setScene(scenes.get("certificates"));
    stage.setTitle("MosregTrader - Выбор сертификата");
    certificateListView.getController().bootstrap();
    stage.showAndWait();
    return Optional.ofNullable(((CertificateListController)certificateListView.getController()).getCertificateInfo());
  }

  public void showMainUI() {
    primaryStage.setScene(scenes.get("main"));
    primaryStage.setTitle("MosregTrader - Главное окно");
    mainView.getController().bootstrap();
    primaryStage.setMaximized(true);
    primaryStage.show();
  }

  public Optional<List<TradeInfoDto>> showTradeFilterUI() {
    Stage stage = new Stage();
    stage.setScene(scenes.get("tradeFilter"));
    stage.setTitle("MosregTrader - Поиск закупок");
    stage.setMaximized(true);
    tradeFilterView.getController().bootstrap();
    stage.showAndWait();
    return Optional.ofNullable(((TradeFilterController)tradeFilterView.getController()).getTrades());
  }

  public Optional<ProposalData> showProposalDataUI() {
    Stage stage = new Stage();
    stage.setScene(scenes.get("proposalData"));
    stage.setTitle("MosregTrader - Данные для заявок");
    proposalDataView.getController().bootstrap();
    stage.showAndWait();
    return Optional.ofNullable(((ProposalController)proposalDataView.getController()).getProposalData());
  }
}
