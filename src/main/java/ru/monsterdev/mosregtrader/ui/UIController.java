package ru.monsterdev.mosregtrader.ui;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public interface UIController {

  /**
   * Вызывается при загрузке fxml-ресурса единожды
   */
  void initialize();

  /**
   * Вызывается каждый раз перед отображением окна
   */
  void bootstrap();

  void bootstrap(Object params);

  static void showErrorMessage(String msg) {
    Alert errorDlg = new Alert(Alert.AlertType.ERROR);
    errorDlg.setTitle("MosregTrader");
    errorDlg.setHeaderText("Ошибка:");
    errorDlg.setContentText(msg);
    errorDlg.showAndWait();
  }
  
  static Optional<ButtonType> showErrorMessageWithChoose(String msg, ButtonType ...buttons) {
    Alert errorDlg = new Alert(Alert.AlertType.ERROR, msg, buttons);
    errorDlg.setTitle("MosregTrader");
    errorDlg.setHeaderText("Ошибка:");
    return errorDlg.showAndWait();
  }

  static void showInfoMessage(String msg) {
    Alert errorDlg = new Alert(Alert.AlertType.INFORMATION);
    errorDlg.setTitle("MosregTrader");
    errorDlg.setHeaderText("Информация:");
    errorDlg.setContentText(msg);
    errorDlg.showAndWait();
  }

  static boolean showConfirmMessage(String msg) {
    Alert confirmDlg = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
    confirmDlg.setTitle("MosregTrader");
    confirmDlg.setHeaderText("Подтверждение:");
    Optional<ButtonType> confirm = confirmDlg.showAndWait();
    return confirm.filter(buttonType -> buttonType == ButtonType.YES).isPresent();
  }
}
