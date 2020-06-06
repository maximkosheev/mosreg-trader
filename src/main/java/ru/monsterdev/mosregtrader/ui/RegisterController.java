package ru.monsterdev.mosregtrader.ui;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.monsterdev.mosregtrader.domain.User;
import ru.monsterdev.mosregtrader.model.CertificateInfo;
import ru.monsterdev.mosregtrader.services.UserService;
import ru.monsterdev.mosregtrader.utils.LicenseUtil;

public class RegisterController extends AbstractUIController {

  @Autowired
  private UserService userService;

  @FXML
  TextField edtName;
  @FXML
  TextField edtCertificate;
  @FXML
  TextField edtEmail;
  @FXML
  TextField edtFax;
  @FXML
  TextField edtPhone;
  @FXML
  TextField edtSurname;
  @FXML
  TextField edtFatherName;
  @FXML
  TextField edtFirstName;
  @FXML
  CheckBox chbNDS;
  @FXML
  ComboBox<Integer> cmbNDS;

  @Autowired
  private UIDispatcher uiDispatcher;

  private CertificateInfo certificateInfo = null;

  @Override
  public void initialize() {
    cmbNDS.getItems().setAll(0, 6, 10, 18, 20);
  }

  @FXML
  private void onCancel() {
    uiDispatcher.showLoginUI();
  }

  @FXML
  private void onSelectCert(ActionEvent event) {
    Optional<CertificateInfo> result = uiDispatcher.showCertificateListUI();
    result.ifPresent(certificate -> {
      this.certificateInfo = certificate;
      edtCertificate.setText(certificate.getName());
    });
  }

  /**
   * Проверяет правильность заполнение полей формы
   *
   * @return true, если заполнено все правильно, false - в противном случае
   */
  private boolean validate() {
    if (edtName.getText().isEmpty()) {
      UIController.showErrorMessage("Поле 'Наименование' не может быть пустым");
      return false;
    }
    if (certificateInfo == null) {
      UIController.showErrorMessage("Поле 'Сертификат' не может быть пустым");
      return false;
    }
    if (edtEmail.getText().isEmpty()) {
      UIController.showErrorMessage("Поле 'Email' не может быть пустым");
      return false;
    }
    if (edtPhone.getText().isEmpty()) {
      UIController.showErrorMessage("Поле 'Телефон' не может быть пустым");
      return false;
    }
    if (edtSurname.getText().isEmpty() || edtFatherName.getText().isEmpty() || edtFirstName.getText().isEmpty()) {
      UIController.showErrorMessage("Поля 'Фамилия', 'Имя', 'Отчество' не могут быть пустыми");
      return false;
    }
    return true;
  }

  @FXML
  private void onRegister(ActionEvent event) {
    if (!LicenseUtil.check(userService.getCount() + 1L)) {
      UIController.showErrorMessage("Вы превысили ограничение по количеству пользователей,\n" +
          "зарегистрированных в системе, установленное вашей лицензией.\n" +
          "Обратитесь к диллеру для получения соответсвующей лицензии");
      return;
    }
    if (validate()) {
      User newUser = new User();
      newUser.setName(edtName.getText());
      newUser.setCertName(certificateInfo.getName());
      newUser.setCertHash(certificateInfo.getHash());
      newUser.setLastName(edtSurname.getText());
      newUser.setFirstName(edtFirstName.getText());
      newUser.setFatherName(edtFatherName.getText());
      newUser.setEmail(edtEmail.getText());
      newUser.setFax(edtFax.getText());
      newUser.setPhone(edtPhone.getText());
      newUser.setUseNDS(chbNDS.isSelected());
      newUser.setNDS(cmbNDS.getValue() != null ? cmbNDS.getValue() : 0);
      userService.register(newUser);
      uiDispatcher.showLoginUI();
    }
  }

}
