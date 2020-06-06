package ru.monsterdev.mosregtrader.ui;

import java.security.UnrecoverableKeyException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.monsterdev.mosregtrader.domain.User;
import ru.monsterdev.mosregtrader.model.CertificateInfo;
import ru.monsterdev.mosregtrader.services.CryptoService;
import ru.monsterdev.mosregtrader.services.UserService;
import ru.monsterdev.mosregtrader.tasks.LoginTask;
import ru.monsterdev.mosregtrader.ui.control.PasswordDialog;
import ru.monsterdev.mosregtrader.ui.control.WaitIndicator;
import ru.monsterdev.mosregtrader.utils.LicenseUtil;

@Slf4j
public class LoginController extends AbstractUIController {

  @FXML
  private StackPane rootPane;
  @FXML
  private GridPane wrapPane;
  @FXML
  ListView<User> lstUsers;
  @FXML
  Hyperlink lnkRegister;
  @FXML
  Button btnLogin;
  @FXML
  Button btnCancel;

  private WaitIndicator waitIndicator;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private UserService userService;

  @Autowired
  private CryptoService cryptoService;

  @Autowired
  private UIDispatcher uiDispatcher;

  @Autowired
  private LoginTask loginTask;

  @Override
  public void initialize() {
    btnCancel.setOnAction(event -> uiDispatcher.close());
    waitIndicator = new WaitIndicator();
  }

  @Override
  public void bootstrap() {
    List<User> users = userService.findAll();

    if (!LicenseUtil.check(users.size())) {
      UIController.showErrorMessage("Превышено ограничение лицензии на количество пользователей");
      return;
    }
    lstUsers.getItems().clear();
    lstUsers.getItems().addAll(users);
  }

  @FXML
  private void onRegister() {
    uiDispatcher.showRegisterUI();
  }

  private boolean loginToMarket(User user) {
    CertificateInfo certInfo = null;
    try {
      certInfo = Objects.requireNonNull(cryptoService.getCertificateByHash(user.getCertHash()),
          "Сертификат не найден. " +
              "Возможно контейнер закрытого ключа не установлен. " +
              "Установите контейнер закрытого ключа и перезапустите приложение.");

      // Цикл до тех пор пока пользователь не введет корректный пароль для загрузки приватного ключа подписи
      while (certInfo.getPrivateKey() == null) {
        try {
          certInfo.loadInfo();
        } catch (UnrecoverableKeyException ex) {
          PasswordDialog dlg = new PasswordDialog();
          dlg.setHeaderText("Введите пароль для контейнера: " + certInfo.getAlias());
          Optional<String> result = dlg.showAndWait();
          if (result.isPresent()) {
            certInfo.setPassword(result.get());
          }
        }
      }
      loginTask.setCertInfo(certInfo);
      loginTask.setUser(user);
      loginTask.setOnFailed(event -> {
        releaseUI();
        UIController.showErrorMessage(loginTask.getException().getMessage());
      });
      loginTask.setOnSucceeded(event -> {
        releaseUI();
        uiDispatcher.showMainUI();
      });
      lockUI();
      loginTask.start();
    } catch (Exception ex) {
      log.error("", ex);
      UIController.showErrorMessage(ex.getMessage());
      return false;
    }
    return true;
  }

  @FXML
  private void onLogin(ActionEvent event) {
    User selectedUser = lstUsers.getSelectionModel().getSelectedItem();
    if (selectedUser == null) {
      UIController.showErrorMessage("Необходимо выбрать пользователя из списка");
      return;
    }
    loginToMarket(selectedUser);
  }

  private void lockUI() {
    wrapPane.setDisable(true);
    rootPane.getChildren().add(waitIndicator);
  }

  private void releaseUI() {
    rootPane.getChildren().remove(waitIndicator);
    wrapPane.setDisable(false);
  }
}
