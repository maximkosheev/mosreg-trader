package ru.monsterdev.mosregtrader.ui;

import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import ru.monsterdev.mosregtrader.domain.User;
import ru.monsterdev.mosregtrader.model.CertificateInfo;
import ru.monsterdev.mosregtrader.services.UserService;
import ru.monsterdev.mosregtrader.utils.LicenseUtil;

public class ProfileController extends AbstractUIController {

  public enum ShowMode {
    REGISTER,
    EDIT
  }

  @Autowired
  private UserService userService;

  @FXML
  GridPane rootPane;
  @FXML
  private Text lblCaption;
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
  TextField edtBankName;
  @FXML
  TextField edtBik;
  @FXML
  TextField edtCorrespondentAccount;
  @FXML
  TextField edtCheckingAccount;
  @FXML
  TextField edtPersonalAccount;

  @FXML
  CheckBox chbNDS;
  @FXML
  ComboBox<Integer> cmbNDS;

  @FXML
  private Button btnOk;
  @FXML
  private Button btnCancel;

  @Autowired
  private UIDispatcher uiDispatcher;

  private CertificateInfo certificateInfo = null;

  private ShowMode showMode;

  @Override
  public void initialize() {
    cmbNDS.getItems().setAll(0, 6, 10, 18, 20);
  }

  @Override
  public void bootstrap(Object params) {
    showMode = (ShowMode) params;
    if (showMode == ShowMode.EDIT) {
      User user = userService.getCurrentUser();

      edtName.setText(user.getName());
      edtCertificate.setText(user.getCertName());
      edtEmail.setText(user.getEmail());
      edtFax.setText(user.getFax());
      edtPhone.setText(user.getPhone());
      edtSurname.setText(user.getLastName());
      edtFirstName.setText(user.getFirstName());
      edtFatherName.setText(user.getFatherName());
      edtBankName.setText(user.getBankName());
      edtBik.setText(user.getBik());
      edtCorrespondentAccount.setText(user.getCorrespondentAccount());
      edtCheckingAccount.setText(user.getCheckingAccount());
      edtPersonalAccount.setText(user.getPersonalAccount());
      chbNDS.setSelected(user.isUseNDS());
      cmbNDS.getSelectionModel().select((Integer) user.getNDS());

      certificateInfo = new CertificateInfo();
      certificateInfo.setName(user.getCertName());
      certificateInfo.setHash(user.getCertHash());
    }

    if (showMode == ShowMode.REGISTER) {
      lblCaption.setText("Регистрация пользователя");
      btnOk.setText("Регистрация");
      btnOk.setOnAction(event -> onRegister());
      btnCancel.setText("Отмена");
      btnCancel.setOnAction(event -> uiDispatcher.showLoginUI());
    } else if (showMode == ShowMode.EDIT) {
      lblCaption.setText("Редактирование пользователя");
      btnOk.setText("Сохранить");
      btnOk.setOnAction(event -> onUpdate());
      btnCancel.setText("Отмена");
      btnCancel.setOnAction(event -> close());
    }
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
    if (edtBankName.getText().isEmpty()) {
      UIController.showErrorMessage("Поле 'Наименование банка' не может быть пустым");
      return false;
    }
    if (edtBik.getText().isEmpty()) {
      UIController.showErrorMessage("Поле 'БИК банка' не может быть пустым");
      return false;
    }
    if (edtCorrespondentAccount.getText().isEmpty()) {
      UIController.showErrorMessage("Поле 'Корреспонденский счёт' не может быть пустым");
      return false;
    }
    if (edtCheckingAccount.getText().isEmpty()) {
      UIController.showErrorMessage("Поле 'Расчетный счёт' не может быть пустым");
      return false;
    }
    return true;
  }

  private void onRegister() {
    if (!LicenseUtil.checkAccountsLimit(userService.getCount() + 1L)) {
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
      newUser.setBankName(edtBankName.getText());
      newUser.setBik(edtBik.getText());
      newUser.setCorrespondentAccount(edtCorrespondentAccount.getText());
      newUser.setCheckingAccount(edtCheckingAccount.getText());
      newUser.setPersonalAccount(edtPersonalAccount.getText());
      newUser.setUseNDS(chbNDS.isSelected());
      newUser.setNDS(cmbNDS.getValue() != null ? cmbNDS.getValue() : 0);
      userService.register(newUser);
      uiDispatcher.showLoginUI();
    }
  }

  private void onUpdate() {
    if (validate()) {
      User currentUser = userService.getCurrentUser();
      currentUser.setName(edtName.getText());
      currentUser.setCertName(certificateInfo.getName());
      currentUser.setCertHash(certificateInfo.getHash());
      currentUser.setLastName(edtSurname.getText());
      currentUser.setFirstName(edtFirstName.getText());
      currentUser.setFatherName(edtFatherName.getText());
      currentUser.setEmail(edtEmail.getText());
      currentUser.setFax(edtFax.getText());
      currentUser.setPhone(edtPhone.getText());
      currentUser.setBankName(edtBankName.getText());
      currentUser.setBik(edtBik.getText());
      currentUser.setCorrespondentAccount(edtCorrespondentAccount.getText());
      currentUser.setCheckingAccount(edtCheckingAccount.getText());
      currentUser.setPersonalAccount(edtPersonalAccount.getText());
      currentUser.setUseNDS(chbNDS.isSelected());
      currentUser.setNDS(cmbNDS.getValue() != null ? cmbNDS.getValue() : 0);
      userService.update();
      close();
    }
  }

  private Stage getStage() {
    return (Stage)rootPane.getScene().getWindow();
  }

  private void close() {
    getStage().close();
  }
}
