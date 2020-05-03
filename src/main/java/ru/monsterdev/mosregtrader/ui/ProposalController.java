package ru.monsterdev.mosregtrader.ui;

import java.math.BigDecimal;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import ru.monsterdev.mosregtrader.model.ProposalData;

@Controller
public class ProposalController extends AbstractUIController {

  @FXML
  private VBox rootPane;
  @FXML
  private TextField edtMinTradeVal;
  @FXML
  private TextField edtStartTradeVal;
  @FXML
  private TextField edtActivateTime;

  private boolean isOK = false;

  private ProposalData proposalData = new ProposalData();

  private Stage getStage() {
    return (Stage)rootPane.getScene().getWindow();
  }

  @FXML
  private void onOk(ActionEvent event) {
    if (!validate()) {
      return;
    }
    isOK = true;
    proposalData.setMinTradeVal(new BigDecimal(edtMinTradeVal.getText()));
    proposalData.setStartTradeVal(StringUtils.isNumeric(edtStartTradeVal.getText()) ?
        new BigDecimal(edtStartTradeVal.getText()) : null);
    long activateTime = Long.parseLong(edtActivateTime.getText());
    if (activateTime == 0) {
      activateTime = Long.MAX_VALUE;
    } else {
      activateTime = activateTime * 60 * 1000;
    }
    proposalData.setActivateTime(activateTime);
    getStage().close();
  }

  @FXML
  private void onCancel(ActionEvent event) {
    isOK = false;
    getStage().close();
  }

  public ProposalData getProposalData() {
    return isOK ? proposalData : null;
  }

  private boolean validate() {
    if (edtMinTradeVal.getText().isEmpty()) {
      UIController.showErrorMessage("Поле 'Минимальное предложение цены (МПЦ)' не может быть пустым");
      return false;
    }
    if (!StringUtils.isNumeric(edtMinTradeVal.getText())) {
      UIController.showErrorMessage("Поле 'Минимальное предложение цены (МПЦ)' должно быть числовым значением");
      return false;
    }
    if (!StringUtils.isEmpty(edtStartTradeVal.getText()) && !StringUtils.isNumeric(edtStartTradeVal.getText())) {
        UIController.showErrorMessage("Поле 'Начальное предложение цены' должно быть числовым значением");
        return false;
    }
    if (edtActivateTime.getText().isEmpty()) {
      edtActivateTime.setText("0");
    }
    if (!StringUtils.isNumeric(edtActivateTime.getText())) {
      UIController.showErrorMessage("Поле 'Время активации' должно быть числовым значением");
      return false;
    }
    return true;
  }
}
