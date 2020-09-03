package ru.monsterdev.mosregtrader.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import ru.monsterdev.mosregtrader.enums.ReducePriceType;
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
  @FXML
  private ToggleGroup reduceType;
  @FXML
  private RadioButton optAbsoluteReduce;
  @FXML
  private RadioButton optRelativeReduce;
  @FXML
  private TextField edtAbsoluteReduceValue;
  @FXML
  private TextField edtRelativeReduceValue;

  private boolean isOK;
  private ReducePriceType reducePriceType;
  private BigDecimal oneHundred = new BigDecimal("100.0");

  private ProposalData proposalData = new ProposalData();

  private Stage getStage() {
    return (Stage)rootPane.getScene().getWindow();
  }

  @Override
  public void initialize() {
    reduceType.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
      RadioButton selectedOption = (RadioButton) newValue;
      if (selectedOption == optAbsoluteReduce) {
        reducePriceType = ReducePriceType.ABSOLUTE;
        edtAbsoluteReduceValue.setDisable(false);
        edtRelativeReduceValue.setDisable(true);
      } else if (selectedOption == optRelativeReduce) {
        reducePriceType = ReducePriceType.RELATIVE;
        edtRelativeReduceValue.setDisable(false);
        edtAbsoluteReduceValue.setDisable(true);
      } else {
        reducePriceType = null;
        edtAbsoluteReduceValue.setDisable(true);
        edtRelativeReduceValue.setDisable(true);
      }
    });
  }

  @Override
  public void bootstrap() {
    isOK = false;
    reduceType = null;
    edtMinTradeVal.clear();
    edtStartTradeVal.clear();
    edtActivateTime.clear();
    edtAbsoluteReduceValue.clear();
    edtRelativeReduceValue.clear();
    optAbsoluteReduce.setSelected(false);
    optRelativeReduce.setSelected(false);
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
    proposalData.setReducePriceType(reducePriceType);
    proposalData.setAbsoluteReduceValue(StringUtils.isNumeric(edtAbsoluteReduceValue.getText()) ?
        new BigDecimal(edtAbsoluteReduceValue.getText()) : null);
    if (StringUtils.isNumeric(edtRelativeReduceValue.getText())) {
      BigDecimal relativeReduceValue = new BigDecimal(edtRelativeReduceValue.getText());
      relativeReduceValue = relativeReduceValue.setScale(2, RoundingMode.HALF_UP);
      proposalData.setRelativeReduceValue(relativeReduceValue.divide(oneHundred, RoundingMode.HALF_UP));
    } else {
      proposalData.setRelativeReduceValue(null);
    }
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
    if (reducePriceType == ReducePriceType.ABSOLUTE) {
      if (StringUtils.isEmpty(edtAbsoluteReduceValue.getText()) || !StringUtils.isNumeric(edtAbsoluteReduceValue.getText())) {
        UIController.showErrorMessage("Поле 'Абсолютное значение' не может быть пустым и должно быть числовым значением");
        return false;
      }
    } else if (reducePriceType == ReducePriceType.RELATIVE) {
      if (StringUtils.isEmpty(edtRelativeReduceValue.getText()) || !StringUtils.isNumeric(edtRelativeReduceValue.getText())) {
        UIController.showErrorMessage("Поле 'Относительное значение' не может быть пустым и должно быть числовым значением");
        return false;
      }
    } else {
      UIController.showErrorMessage("Параметр 'Снижение цены' должен быть задан");
      return false;
    }
    return true;
  }
}
