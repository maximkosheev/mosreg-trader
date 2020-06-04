package ru.monsterdev.mosregtrader.ui;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.monsterdev.mosregtrader.domain.FilterOption;
import ru.monsterdev.mosregtrader.enums.FilterType;
import ru.monsterdev.mosregtrader.enums.SourcePlatformType;
import ru.monsterdev.mosregtrader.enums.TradeStatus;
import ru.monsterdev.mosregtrader.model.dto.TradeFilterDto;
import ru.monsterdev.mosregtrader.model.dto.TradeInfoDto;
import ru.monsterdev.mosregtrader.model.dto.TradesInfoDto;
import ru.monsterdev.mosregtrader.services.DictionaryService;
import ru.monsterdev.mosregtrader.tasks.GetFilteredTradesTask;
import ru.monsterdev.mosregtrader.ui.control.MultilineCellFactory;
import ru.monsterdev.mosregtrader.ui.control.WaitIndicator;
import ru.monsterdev.mosregtrader.ui.model.TradeViewItem;
import ru.monsterdev.mosregtrader.utils.StringUtil;

@Slf4j
public class TradeFilterController extends AbstractUIController {

  private static final String START_MSG = "Нет данных для отображения";
  private static final String PENDING_MSG = "Ожидайте, идет обновление...";
  @FXML
  private StackPane contentPane;
  @FXML
  private VBox wrapPane;
  @FXML
  private Label lblTradesCount;
  @FXML
  private TextField edtTradeNum;
  @FXML
  private TextField edtCustomerName;
  @FXML
  private TextField edtEASUZNum;
  @FXML
  private TextField edtCustomerLoc;
  @FXML
  private TextField edtTradeName;
  @FXML
  private DatePicker dateStartFrom;
  @FXML
  private DatePicker dateStartTo;
  @FXML
  private DatePicker dateEndFrom;
  @FXML
  private DatePicker dateEndTo;
  @FXML
  private TextField edtSummMin;
  @FXML
  private TextField edtSummMax;
  @FXML
  private TextField edtKOZ;
  @FXML
  private ComboBox<TradeStatus> cmbStatus;
  @FXML
  private ComboBox<FilterOption> cmbFilters;
  @FXML
  private TableView<TradeViewItem> tblTrades;
  @FXML
  private Pagination pagination;
  @FXML
  private ComboBox<Integer> cmbItemsPerPage;
  private WaitIndicator waitIndicator;

  @Autowired
  private DictionaryService dictionaryService;

  @Autowired
  private ApplicationContext context;

  private DateTimeFormatter onlyDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private DateTimeFormatter withTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

  private boolean isOK = false;

  private ChangeListener<Number> currentPageChangeListener;

  List<TradeInfoDto> selectedTrades = new ArrayList<>();

  @Override
  public void bootstrap() {
    lblTradesCount.setText(START_MSG);
    cmbFilters.setOnAction(value -> {
      FilterOption filter = cmbFilters.getValue();
      if (filter != null) {
        clearFilter(true);
        setFilterFields(filter.getFields());
      }
    });
    cmbFilters.getItems().addAll(dictionaryService.findAllFilters(FilterType.TRADE));
    cmbStatus.getItems().addAll(TradeStatus.allFromMosreg());
    cmbStatus.getSelectionModel().select(0);
    cmbItemsPerPage.getItems().addAll(5, 10, 20, 50, 100);
    cmbItemsPerPage.setValue(100);
    cmbItemsPerPage.setOnAction(event -> doFilter(0));

    CheckBox chbSelectAll = new CheckBox();
    chbSelectAll.selectedProperty().addListener((observable, oldValue, newValue) ->
        tblTrades.getItems().forEach(trade -> trade.setSelected(newValue)));

    TableColumn<TradeViewItem, Boolean> c0 = new TableColumn<>("");
    c0.setCellValueFactory(new PropertyValueFactory<>("selected"));
    c0.setCellFactory(CheckBoxTableCell.forTableColumn(c0));
    c0.setGraphic(chbSelectAll);
    c0.setPrefWidth(34);

    TableColumn<TradeViewItem, Long> c1 = new TableColumn<>("Номер\nзакупки");
    c1.setCellValueFactory(new PropertyValueFactory<>("id"));
    c1.setStyle("-fx-alignment: CENTER;");
    c1.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.05));

    TableColumn<TradeViewItem, String> c2 = new TableColumn<>("Заказчик");
    c2.setCellValueFactory(new PropertyValueFactory<>("customer"));
    c2.setCellFactory(new MultilineCellFactory());
    c2.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.337));

    TableColumn<TradeViewItem, String> c3 = new TableColumn<>("Наименование закупки");
    c3.setCellValueFactory(new PropertyValueFactory<>("name"));
    c3.setCellFactory(new MultilineCellFactory());
    c3.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.305));

    TableColumn<TradeViewItem, String> c4 = new TableColumn<>("НМЦ");
    c4.setCellValueFactory(new PropertyValueFactory<>("initialPrice"));
    c4.setCellValueFactory(
        param -> new SimpleStringProperty(param.getValue().getInitialPrice().setScale(2, RoundingMode.UP).toString()));
    c4.setStyle("-fx-alignment: CENTER;");
    c4.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.05));

    TableColumn<TradeViewItem, String> c5 = new TableColumn<>("Дата и время\nначала подачи\nпредложений");
    c5.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPublicationDate().format(onlyDate)));
    c5.setStyle("-fx-alignment: CENTER;");
    c5.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.073));

    TableColumn<TradeViewItem, String> c6 = new TableColumn<>("Дата и время\nокончания подачи\nпредложений");
    c6.setCellValueFactory(new PropertyValueFactory<>("fillingApplicationEndDate"));
    c6.setCellValueFactory(
        param -> new SimpleStringProperty(param.getValue().getFillingApplicationEndDate().format(withTime)));
    c6.setStyle("-fx-alignment: CENTER;");
    c6.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.073));

    TableColumn<TradeViewItem, String> c7 = new TableColumn<>("Статус");
    c7.setCellValueFactory(new PropertyValueFactory<>("stateName"));
    c7.setStyle("-fx-alignment: CENTER;");
    c7.prefWidthProperty().bind(tblTrades.widthProperty().subtract(34).multiply(0.097));

    currentPageChangeListener = (observable, oldValue, newValue) -> doFilter(newValue.intValue());
    tblTrades.getColumns().addAll(c0, c1, c2, c3, c4, c5, c6, c7);
    waitIndicator = new WaitIndicator();

    onFilterApply();
  }

  public List<TradeInfoDto> getTrades() {
    if (isOK) {
      return selectedTrades;
    } else {
      return null;
    }
  }

  private void doFilter(int page) {
    TradeFilterDto filter = new TradeFilterDto();

    String classificatorCodes = edtKOZ.getText();
    if (!StringUtils.isEmpty(classificatorCodes)) {
      filter.setClassificatorCodes(Arrays.asList(classificatorCodes.split(",")));
    }
    filter.setCustomerAddress(edtCustomerLoc.getText());
    filter.setCustomerFullNameOrInn(edtCustomerName.getText());
    if (dateStartFrom.getValue() != null) {
      filter.setFilterDateFrom(dateStartFrom.getValue().atTime(0, 0, 0));
    }
    if (dateStartTo.getValue() != null) {
      filter.setFilterDateTo(dateStartTo.getValue().atTime(23, 59, 59));
    }
    if (dateEndFrom.getValue() != null) {
      filter.setFilterFillingApplicationEndDateFrom(dateEndFrom.getValue().atTime(0, 0, 0));
    }
    if (dateEndTo.getValue() != null) {
      filter.setFilterFillingApplicationEndDateTo(dateEndTo.getValue().atTime(23, 59, 59));
    }
    filter.setFilterPriceMin(StringUtil.toLong(edtSummMin.getText()));
    filter.setFilterPriceMax(StringUtil.toLong(edtSummMax.getText()));
    filter.setFilterTradeEasuzNumber(edtEASUZNum.getText());
    filter.setIsImmediate(false);
    filter.setItemsPerPage(cmbItemsPerPage.getValue());
    filter.setOnlyTradesWithMyApplications(false);
    filter.setPage(page + 1);
    filter.setParticipantHasApplicationsOnTrade("");
    filter.setShowOnlyOwnTrades(false);
    filter.setSortingParams(Collections.emptyList());
    filter.setId(StringUtil.toLong(edtTradeNum.getText()));
    filter.setTradeName(edtTradeName.getText());
    if (cmbStatus.getValue().getCode() > 0) {
      filter.setTradeState(cmbStatus.getValue().getCode());
    }
    filter.setUsedClassificatorType(10);
    filter.setSourcePlatform(SourcePlatformType.EASUZ);

    GetFilteredTradesTask filterTradesTask = context.getBean(GetFilteredTradesTask.class, filter);
    filterTradesTask.setOnFailed(event1 -> {
      log.error("", filterTradesTask.getException());
      releaseUI();
      UIController.showErrorMessage(filterTradesTask.getException().getMessage());
    });
    filterTradesTask.setOnSucceeded(event1 -> {
      releaseUI();
      TradesInfoDto tradesInfoList = filterTradesTask.getValue();
      ObservableList<TradeViewItem> items = FXCollections.observableArrayList();
      for (TradeInfoDto tradeInfo : tradesInfoList.getTrades()) {
        items.add(new TradeViewItem(tradeInfo));
      }
      tblTrades.setItems(items);
      lblTradesCount.setText(String.valueOf(tradesInfoList.getTotalrecords()));
      pagination.setPageCount(tradesInfoList.getTotalpages() > 0 ? tradesInfoList.getTotalpages() : 1);
      pagination.currentPageIndexProperty().removeListener(currentPageChangeListener);
      // setCurrentPageIndex генерирует событие CHANGE, на которое повешен слушатель currentPageChangeListener,
      // который отправлят запрос на получения списка закупок. При обработке этого списка снова задается CurrentPageIndex
      // и так далее до бесконечности. Чтобы этого избежать, перед указанием CurrentPageIndex сначала удаляем слушатель,
      // а затем снова его добавляем
      pagination.setCurrentPageIndex(tradesInfoList.getCurrpage() - 1);
      pagination.currentPageIndexProperty().addListener(currentPageChangeListener);
      tblTrades.refresh();
    });
    lblTradesCount.setText(PENDING_MSG);
    lockUI();
    filterTradesTask.start();
  }

  private void clearFilter(boolean onlyFields) {
    edtTradeNum.clear();
    edtCustomerName.clear();
    edtEASUZNum.clear();
    edtCustomerLoc.clear();
    edtTradeName.clear();
    dateStartFrom.setValue(null);
    dateStartTo.setValue(null);
    dateEndFrom.setValue(null);
    dateEndTo.setValue(null);
    edtSummMin.clear();
    edtSummMax.clear();
    edtKOZ.clear();
    cmbStatus.getSelectionModel().select(0);
    if (!onlyFields) {
      cmbFilters.setValue(null);
    }
  }

  private void setFilterFields(Map<String, String> fields) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    edtTradeNum.setText(fields.getOrDefault("TradeNum", ""));
    edtCustomerName.setText(fields.getOrDefault("CustomerName", ""));
    edtEASUZNum.setText(fields.getOrDefault("EASUZNum", ""));
    edtCustomerLoc.setText(fields.getOrDefault("CustomerLoc", ""));
    edtTradeName.setText(fields.getOrDefault("TradeName", ""));
    dateStartFrom
        .setValue(fields.containsKey("StartFrom") ? LocalDate.parse(fields.get("StartFrom"), formatter) : null);
    dateStartTo.setValue(fields.containsKey("StartTo") ? LocalDate.parse(fields.get("StartTo"), formatter) : null);
    dateEndFrom.setValue(fields.containsKey("EndFrom") ? LocalDate.parse(fields.get("EndFrom"), formatter) : null);
    dateEndTo.setValue(fields.containsKey("EndTo") ? LocalDate.parse(fields.get("EndTo"), formatter) : null);
    edtSummMin.setText(fields.getOrDefault("SummMin", ""));
    edtSummMax.setText(fields.getOrDefault("SummMax", ""));
    edtKOZ.setText(fields.getOrDefault("KOZ", ""));
    cmbStatus.getSelectionModel().select(0);
    for (TradeStatus status : cmbStatus.getItems()) {
      if (status.getCode() == Integer.parseInt(fields.getOrDefault("Status", "0"))) {
        cmbStatus.setValue(status);
      }
    }
  }

  @FXML
  private void onKOZSelect() {
    //
  }

  @FXML
  private void onOk() {
    selectedTrades.clear();
    selectedTrades.addAll(tblTrades.getItems().stream()
        .filter(TradeViewItem::isSelected)
        .map(TradeViewItem::getInfo)
        .collect(Collectors.toList())
    );
    if (selectedTrades.isEmpty()) {
      UIController.showErrorMessage("Не выбрано ни одной закупки");
      return;
    }
    isOK = true;
    close();
  }

  @FXML
  private void onCancel() {
    isOK = false;
    close();
  }

  private void lockUI() {
    wrapPane.setDisable(true);
    contentPane.getChildren().add(waitIndicator);
  }

  private void releaseUI() {
    contentPane.getChildren().remove(waitIndicator);
    wrapPane.setDisable(false);
  }

  @FXML
  private void onFilterClear() {
    clearFilter(false);
  }

  @FXML
  private void onFilterApply() {
    doFilter(0);
  }

  @FXML
  private void onFilterSaveAs() {
    TextInputDialog dlg = new TextInputDialog();
    dlg.setTitle("MosregTrader");
    dlg.setHeaderText("Имя фильтра (под этим именем фильтр будет сохранен в БД)");
    dlg.setContentText("имя фильтра (не более 50 символов)");
    String filterName = dlg.showAndWait().get();
    if (filterName.isEmpty()) {
      UIController.showErrorMessage("Вы должны задать имя фильтра");
      return;
    }
    if (filterName.length() > 50) {
      UIController.showErrorMessage("Длина имени фильтра не может превышать 50 символов");
      return;
    }

    Map<String, String> fields = new HashMap<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    if (!edtTradeNum.getText().isEmpty()) {
      fields.put("TradeNum", FilterOption.prepareString(edtTradeNum.getText()));
    }
    if (!edtEASUZNum.getText().isEmpty()) {
      fields.put("EASUZNum", FilterOption.prepareString(edtEASUZNum.getText()));
    }
    if (!edtTradeName.getText().isEmpty()) {
      fields.put("TradeName", FilterOption.prepareString(edtTradeName.getText()));
    }
    if (!edtCustomerName.getText().isEmpty()) {
      fields.put("CustomerName", FilterOption.prepareString(edtCustomerName.getText()));
    }
    if (!edtCustomerLoc.getText().isEmpty()) {
      fields.put("CustomerLoc", FilterOption.prepareString(edtCustomerLoc.getText()));
    }
    if (!edtSummMin.getText().isEmpty()) {
      fields.put("SummMin", FilterOption.prepareString(edtSummMin.getText()));
    }
    if (!edtSummMax.getText().isEmpty()) {
      fields.put("SummMax", FilterOption.prepareString(edtSummMax.getText()));
    }
    if (dateStartFrom.getValue() != null) {
      fields.put("StartFrom", dateStartFrom.getValue().format(formatter));
    }
    if (dateStartTo.getValue() != null) {
      fields.put("StartTo", dateStartTo.getValue().format(formatter));
    }
    if (dateEndFrom.getValue() != null) {
      fields.put("EndFrom", dateEndFrom.getValue().format(formatter));
    }
    if (dateEndTo.getValue() != null) {
      fields.put("EndTo", dateEndTo.getValue().format(formatter));
    }
    if (!edtKOZ.getText().isEmpty()) {
      fields.put("KOZ", FilterOption.prepareString(edtKOZ.getText()));
    }
    if (cmbStatus.getValue() != null) {
      fields.put("Status", String.valueOf(cmbStatus.getValue().getCode()));
    }

    FilterOption filter = new FilterOption();
    filter.setName(filterName);
    filter.setFields(fields);
    dictionaryService.saveFilter(FilterType.TRADE, filter);
    cmbFilters.getItems().add(filter);
    cmbFilters.setValue(filter);
  }

  private Stage getStage() {
    return (Stage)contentPane.getScene().getWindow();
  }

  private void close() {
    getStage().close();
  }
}
