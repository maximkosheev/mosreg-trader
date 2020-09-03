package ru.monsterdev.mosregtrader.ui;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.monsterdev.mosregtrader.domain.User;
import ru.monsterdev.mosregtrader.model.CertificateInfo;
import ru.monsterdev.mosregtrader.services.UserService;

public class EditProfileController extends AbstractUIController {

  @Autowired
  private UserService userService;

  @FXML
  GridPane rootPane;
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

  @Override
  public void bootstrap() {
    User user = userService.getCurrentUser();

    edtName.setText(user.getName());
    edtCertificate.setText(user.getCertName());
    edtEmail.setText(user.getEmail());
    edtFax.setText(user.getFax());
    edtPhone.setText(user.getPhone());
    edtSurname.setText(user.getLastName());
    edtFirstName.setText(user.getFirstName());
    edtFatherName.setText(user.getFatherName());
    chbNDS.setSelected(user.isUseNDS());
    cmbNDS.getSelectionModel().select((Integer) user.getNDS());

    certificateInfo = new CertificateInfo();
    certificateInfo.setName(user.getCertName());
    certificateInfo.setHash(user.getCertHash());
  }

  @FXML
  private void onSelectCert() {
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
  private void onUpdate() {
    if (validate()) {
      userService.getCurrentUser().setName(edtName.getText());
      userService.getCurrentUser().setCertName(certificateInfo.getName());
      userService.getCurrentUser().setCertHash(certificateInfo.getHash());
      userService.getCurrentUser().setLastName(edtSurname.getText());
      userService.getCurrentUser().setFirstName(edtFirstName.getText());
      userService.getCurrentUser().setFatherName(edtFatherName.getText());
      userService.getCurrentUser().setEmail(edtEmail.getText());
      userService.getCurrentUser().setFax(edtFax.getText());
      userService.getCurrentUser().setPhone(edtPhone.getText());
      userService.getCurrentUser().setUseNDS(chbNDS.isSelected());
      userService.getCurrentUser().setNDS(cmbNDS.getValue() != null ? cmbNDS.getValue() : 0);
      userService.update();
      close();
    }
  }

  @FXML
  private void onCancel() {
    close();
  }

  private Stage getStage() {
    return (Stage)rootPane.getScene().getWindow();
  }

  private void close() {
    getStage().close();
  }
}
