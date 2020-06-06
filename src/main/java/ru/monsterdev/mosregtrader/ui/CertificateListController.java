package ru.monsterdev.mosregtrader.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.monsterdev.mosregtrader.model.CertificateInfo;
import ru.monsterdev.mosregtrader.services.CryptoService;

@Controller
public class CertificateListController extends AbstractUIController {

  @FXML
  private Pane rootPane;
  @FXML
  private TableView<CertificateInfo> tblCertificates;

  // текущий выбранный сертификат
  private CertificateInfo certificateInfo = null;

  @Autowired
  private CryptoService cryptoService;

  @Override
  public void initialize() {
    TableColumn<CertificateInfo, String> c1 = new TableColumn<>("Название");
    TableColumn<CertificateInfo, String> c2 = new TableColumn<>("Срок действия");
    c1.setCellValueFactory(new PropertyValueFactory<>("name"));
    c2.setCellValueFactory(new PropertyValueFactory<>("validity"));
    c1.prefWidthProperty().bind(tblCertificates.widthProperty().multiply(0.5));
    c2.prefWidthProperty().bind(tblCertificates.widthProperty().multiply(0.5));
    tblCertificates.getColumns().setAll(c1, c2);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void bootstrap() {
    tblCertificates.getItems().clear();
    tblCertificates.getItems().setAll(cryptoService.getCertificatesList());
  }

  private Stage getStage() {
    return (Stage) rootPane.getScene().getWindow();
  }

  public CertificateInfo getCertificateInfo() {
    return certificateInfo;
  }

  @FXML
  private void onOk() {
    certificateInfo = tblCertificates.getSelectionModel().getSelectedItem();
    if (certificateInfo == null) {
      UIController.showErrorMessage("Необходимо выбрать сертификат");
      return;
    }
    getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
  }
}
