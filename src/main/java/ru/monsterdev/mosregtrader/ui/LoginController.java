package ru.monsterdev.mosregtrader.ui;

import java.security.UnrecoverableKeyException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.monsterdev.mosregtrader.domain.User;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
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
  Label lblUsersCount;
  @FXML
  Label lblUntil;
  @FXML
  Button btnLogin;
  @FXML
  Button btnCancel;

  private WaitIndicator waitIndicator;

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

    lblUsersCount.setText(String.valueOf(LicenseUtil.getAccountsLimit()));
    lblUntil.setText(LicenseUtil.getUntilLimit().toString());
    if (!LicenseUtil.checkAccountsLimit(users.size())) {
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

  private CertificateInfo waitCertificateAvailable(String certHash, String certName) {
    CertificateInfo certificateInfo = cryptoService.getCertificateByHash(certHash);
    while (certificateInfo == null) {
      ButtonType choose = UIController.showErrorMessageWithChoose("Сертификат " + certName + " не найден. " +
              "Возможно контейнер закрытого ключа не установлен. " +
              "Установите контейнер закрытого ключа и нажмите кнопку ОК.",
          ButtonType.OK, ButtonType.CANCEL).orElse(ButtonType.CANCEL);
      if (choose == ButtonType.OK) {
        cryptoService.reloadCertificateInfos();
        certificateInfo = cryptoService.getCertificateByHash(certHash);
      } else {
        return null;
      }
    }
    return certificateInfo;
  }

  private void loginToMarket(User user) {
    try {
      if (!LicenseUtil.checkDateLimit(LocalDate.now())) {
        throw new MosregTraderException("Закончился срок действия лицензии, обратитесь к дилеру для продления");
      }
      CertificateInfo certInfo = waitCertificateAvailable(user.getCertHash(), user.getCertName());
      if (certInfo == null) {
        return;
      }
      // Цикл до тех пор пока пользователь не введет корректный пароль для загрузки приватного ключа подписи
      while (certInfo.getPrivateKey() == null) {
        try {
          certInfo.loadInfo();
        } catch (UnrecoverableKeyException ex) {
          PasswordDialog dlg = new PasswordDialog();
          dlg.setHeaderText("Введите пароль для контейнера: " + certInfo.getAlias());
          Optional<String> result = dlg.showAndWait();
          result.ifPresent(certInfo::setPassword);
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
    } catch (MosregTraderException ex) {
      log.error("", ex);
      UIController.showErrorMessage(ex.getMessage());
    } catch (Exception ex) {
      log.error("", ex);
      UIController.showErrorMessage("Произошла ошибка входа в систему, попробуйте перезапустить приложение");
    }
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
