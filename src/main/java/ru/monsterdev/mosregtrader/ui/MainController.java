package ru.monsterdev.mosregtrader.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;
import ru.monsterdev.mosregtrader.domain.FilterOption;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.enums.FilterType;
import ru.monsterdev.mosregtrader.enums.TradeStatus;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.model.ProposalData;
import ru.monsterdev.mosregtrader.model.StatusFilterOption;
import ru.monsterdev.mosregtrader.model.dto.TradeInfoDto;
import ru.monsterdev.mosregtrader.services.DictionaryService;
import ru.monsterdev.mosregtrader.services.TradeService;
import ru.monsterdev.mosregtrader.tasks.FetchTradesInfoTask;
import ru.monsterdev.mosregtrader.tasks.SubmitProposalTask;
import ru.monsterdev.mosregtrader.ui.control.WaitIndicator;

@Slf4j
public class MainController extends AbstractUIController implements Observer {

  private static final String ERROR_COMMON_LOG_MSG = "Failed to complete operation due error %s : %s";
  private static final String PENDING_MSG = "Ожидайте, идет обновление...";
  private static final String IDLE_MSG = "Простой";

  private static final Integer UPDATE_MAIN_VIEW_TICK = 60 * 1000;

  @FXML
  private StackPane rootPane;
  @FXML
  private BorderPane wrapPane;
  @FXML
  private Label lblTradesCount;
  @FXML
  private DatePicker dateBeginFrom;
  @FXML
  private DatePicker dateBeginTo;
  @FXML
  private TextField edtTradeNum;
  @FXML
  private TextField edtTradeName;
  @FXML
  private ComboBox<StatusFilterOption> cmbStatus;
  @FXML
  private DatePicker dateFinishFrom;
  @FXML
  private DatePicker dateFinishTo;
  @FXML
  private ComboBox<FilterOption> cmbFilters;
  @FXML
  private WebView mainView;
  @FXML
  private Pagination pages;
  @FXML
  private ComboBox<Integer> cmbItemsPerPage;
  @FXML
  private Label lblStatusText;
  private WaitIndicator waitIndicator;
  private WebEngine webEngine;
  private ChangeListener<Number> currentPageChangeListener;
  @Autowired
  private DictionaryService dictionaryService;
  @Autowired
  private UIDispatcher uiDispatcher;
  private Map<String, Object> filterOptions = new Hashtable<>();
  @Autowired
  private TradeService tradeService;
  @Autowired
  private FetchTradesInfoTask fetchTradesInfoTask;
  @Autowired
  private ThreadPoolTaskScheduler threadPoolTaskScheduler;
  @Autowired
  private SubmitProposalTask submitProposalTask;

  @Override
  public void bootstrap() {
    fetchTradesInfoTask.setOnFailed(event -> {
      releaseUI();
      UIController.showErrorMessage(fetchTradesInfoTask.getException().getMessage());
    });
    fetchTradesInfoTask.setOnSucceeded(event -> {
      releaseUI();
      refreshProposals(fetchTradesInfoTask.getValue(), filterOptions);
      threadPoolTaskScheduler.scheduleWithFixedDelay(submitProposalTask, 60 * 1000);
    });

    webEngine = mainView.getEngine();
    webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == Worker.State.SUCCEEDED) {
        lblStatusText.setText(PENDING_MSG);
        fetchTradesInfoTask.start();
      }
    });
    JSObject window = (JSObject) webEngine.executeScript("window");
    window.setMember("app", this);

    lblStatusText.setText(IDLE_MSG);
    cmbFilters.setOnAction(value -> {
      FilterOption filter = cmbFilters.getValue();
      if (filter != null) {
        clearFilter(true);
        setFilterFields(filter.getFields());
      }
    });
    cmbFilters.getItems().addAll(dictionaryService.findAllFilters(FilterType.PROPOSAL));

    cmbStatus.getItems().addAll(
        new StatusFilterOption(StatusFilterOption.ALL, "Все"),
        new StatusFilterOption(StatusFilterOption.OPENED, "Открытые закупки"),
        new StatusFilterOption(StatusFilterOption.CLOSED, "Закрытые закупки"),
        new StatusFilterOption(StatusFilterOption.ACTIVE, "Активные"),
        new StatusFilterOption(StatusFilterOption.ARCHIVED, "Архивные")
    );
    cmbStatus.getSelectionModel().select(0);

    cmbItemsPerPage.getItems().addAll(5, 10, 20, 50, 100);
    cmbItemsPerPage.setValue(10);
    cmbItemsPerPage.setOnAction(event -> doApplyFilter());

    webEngine.load(getClass().getResource("/ru/monsterdev/mosregtrader/ui/mainView.html").toExternalForm());

    waitIndicator = new WaitIndicator();

    currentPageChangeListener = (observable, oldValue, newValue) -> doApplyFilter();
    pages.currentPageIndexProperty().addListener(currentPageChangeListener);
  }

  private void prepareFilterOptions() {
    filterOptions.clear();

    if (!StringUtils.isEmpty(edtTradeNum.getText())) {
      filterOptions.put("TradeNum", Long.parseLong(edtTradeNum.getText()));
    }
    if (!StringUtils.isEmpty(edtTradeName.getText())) {
      filterOptions.put("TradeName", edtTradeName.getText());
    }
    if (dateBeginFrom.getValue() != null) {
      filterOptions.put("BeginFrom", dateBeginFrom.getValue());
    }
    if (dateBeginTo.getValue() != null) {
      filterOptions.put("BeginTo", dateBeginTo.getValue());
    }
    if (dateFinishFrom.getValue() != null) {
      filterOptions.put("FinishFrom", dateFinishFrom.getValue());
    }
    if (dateFinishTo.getValue() != null) {
      filterOptions.put("FinishTo", dateFinishTo.getValue());
    }
    if (cmbStatus.getValue() != null) {
      filterOptions.put("Status", cmbStatus.getValue());
    }
  }

  /**
   * Выполняет поиск предложений в БД, удовлетворяющих параметрам фильтра и отображает их на экране
   */
  private void refreshProposals(List<Trade> trades, Map<String, Object> filterOptions) {
    lockUI();
    int countPerPage = cmbItemsPerPage.getValue();
    int startIndex = pages.getCurrentPageIndex() * countPerPage;
    try {
      lblTradesCount.setText(String.valueOf(trades.size()));
      /*
      List<Trade> trades = tradesRepository.findAll(userService.getCurrentUser().getId(), filterOptions);
      List<TradeProposalItem> items = trades.stream()
          .skip(startIndex)
          .limit(startIndex + countPerPage)
          .map(TradeProposalItem::new)
          .collect(Collectors.toList());
          */
      //JSObject mainView = (JSObject) webEngine.executeScript("mainView");
      //mainView.call("clearProposals");
      //mainView.call("showProposals", items);
      //pages.setPageCount(Math.floorDiv(trades.size(), countPerPage) + 1);
      //lblTradesCount.setText(String.valueOf(trades.size()));
    } catch (Throwable t) {
      t.printStackTrace();
      pages.setPageCount(1);
      lblTradesCount.setText(String.valueOf(0));
    } finally {
      releaseUI();
    }
  }

  @Override
  public void update(Observable o, Object arg) {
    doApplyFilter();
  }

  @FXML
  private void onFileClose() {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("AutoMosreg");
    confirm.setHeaderText("Завершить работу приложения?");
    confirm.setContentText("Работа приложения для текущего пользователя будет полностью остановлена");
    if (confirm.showAndWait().get() == ButtonType.OK) {
      uiDispatcher.close();
    }
  }

  @FXML
  private void onTradeRefresh() {
    doApplyFilter();
  }

  @FXML
  private void onTradeNew() {
    Optional<List<TradeInfoDto>> tradeInfoList = uiDispatcher.showTradeFilterUI();
    if (!tradeInfoList.isPresent()) {
      return;
    }

    // запрашиваем у пользователя ограничения по контракту и время активации предложения
    Optional<ProposalData> optProposalData = uiDispatcher.showProposalDataUI();
    if (!optProposalData.isPresent()) {
      return;
    }

    ProposalData proposalData = optProposalData.get();
    try {
      for (TradeInfoDto tradeInfo : tradeInfoList.get()) {
        Trade trade = new Trade();
        trade.setTradeId(tradeInfo.getId());
        trade.setName(tradeInfo.getTradeName());
        trade.setBeginDT(tradeInfo.getPublicationDate());
        trade.setEndDT(tradeInfo.getFillingApplicationEndDate());
        trade.setNmc(tradeInfo.getInitialPrice());
        trade.setStatus(TradeStatus.valueOf(tradeInfo.getTradeState()));
        trade.setActivateTime(proposalData.getActivateTime());
        trade.setMinTradeVal(proposalData.getMinTradeVal());
        trade.setStartPrice(Objects.isNull(proposalData.getStartTradeVal()) ? tradeInfo.getInitialPrice()
            : proposalData.getStartTradeVal());
        tradeService.addTrade(trade);
      }
    } catch (Throwable t) {
      log.error(String.format(ERROR_COMMON_LOG_MSG, t.getClass(), t.getMessage()));
      UIController.showErrorMessage(t.getMessage());
    }
  }

  @FXML
  private void onTradeEdit() {
    /*
    try {
      Optional<ProposalData> proposalData = uiDispatcher.showProposalDataUI();
      if (!proposalData.isPresent()) {
        return;
      }
      JSObject mainView = (JSObject) webEngine.executeScript("mainView");
      String result = mainView.call("getSelected").toString();
      String[] ids = result.split(",");
      for (String id : ids) {
        Trade trade = userService.getCurrentUser().getTrade(Long.parseLong(id));
        trade.setMinTradeVal(proposalData.get().getMinTradeVal());
        trade.setActivateTime(proposalData.get().getActivateTime());
      }
      //userService.update();
      doApplyFilter();
    } catch (Throwable t) {
      UIController.showErrorMessage(t.getMessage());
    }
    */
  }

  @FXML
  private void onTradeDelete() {
    try {
      JSObject mainView = (JSObject) webEngine.executeScript("mainView");
      String result = mainView.call("getSelected").toString();
      if (result.isEmpty()) {
        throw new MosregTraderException("Выберете закупки, которые нужно удалить, а затем повторите операцию");
      }

      if (!UIController.showConfirmMessage("Вы действительно хотите удалить выбранные закупки?")) {
        return;
      }
      String[] ids = result.split(",");
      for (String id : ids) {
        //userService.getCurrentUser().removeTrade(Long.parseLong(id));
      }
      //userService.update();
      doApplyFilter();
    } catch (Throwable t) {
      UIController.showErrorMessage(t.getMessage());
    }
  }

  /*
  public void onOpenTradePage(Integer tradeId) {
    if (tradeId != null) {
      HostServicesFactory.getInstance(MosregTraderApplication.getInstance())
          .showDocument("https://market.mosreg.ru/Trade/ViewTrade/" + tradeId);
    } else {
      UIController.showErrorMessage("Ошибка при попытке открыть окно браузера по данной закупке");
    }
  }
  */

  @FXML
  private void onTenderStart() {
  }

  @FXML
  private void onTenderStop() {
    //
  }

  @FXML
  private void onProfile() {
    //EditProfileController.showUI();
  }

  @FXML
  private void onAbout() {
    //
  }

  private void doApplyFilter() {
    prepareFilterOptions();
    pages.currentPageIndexProperty().removeListener(currentPageChangeListener);
    pages.setCurrentPageIndex(0);
    pages.currentPageIndexProperty().addListener(currentPageChangeListener);
    webEngine.reload();
  }

  @FXML
  private void onFilterApply() {
    doApplyFilter();
  }

  @FXML
  private void onFilterSaveAs() {
    TextInputDialog dlg = new TextInputDialog();
    dlg.setTitle("AutoMosreg");
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
    if (!edtTradeName.getText().isEmpty()) {
      fields.put("TradeName", FilterOption.prepareString(edtTradeName.getText()));
    }
    if (dateBeginFrom.getValue() != null) {
      fields.put("BeginFrom", dateBeginFrom.getValue().format(formatter));
    }
    if (dateBeginTo.getValue() != null) {
      fields.put("BeginTo", dateBeginTo.getValue().format(formatter));
    }
    if (dateFinishFrom.getValue() != null) {
      fields.put("FinishFrom", dateFinishFrom.getValue().format(formatter));
    }
    if (dateFinishTo.getValue() != null) {
      fields.put("FinishTo", dateFinishTo.getValue().format(formatter));
    }
    if (cmbStatus.getValue() != null) {
      fields.put("Status", String.valueOf(cmbStatus.getValue().getCode()));
    }

    FilterOption filter = new FilterOption();
    filter.setName(filterName);
    filter.setFields(fields);
    dictionaryService.saveFilter(FilterType.PROPOSAL, filter);
    cmbFilters.getItems().add(filter);
    cmbFilters.setValue(filter);
  }

  private void clearFilter(boolean onlyFields) {
    edtTradeNum.clear();
    edtTradeName.clear();
    dateBeginFrom.setValue(null);
    dateBeginTo.setValue(null);
    dateFinishFrom.setValue(null);
    dateFinishTo.setValue(null);
    cmbStatus.getSelectionModel().select(0);
    if (!onlyFields) {
      cmbFilters.getSelectionModel().clearSelection();
    }
  }

  private void setFilterFields(Map<String, String> fields) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    edtTradeNum.setText(fields.getOrDefault("TradeNum", ""));
    edtTradeName.setText(fields.getOrDefault("TradeName", ""));
    dateBeginFrom
        .setValue(fields.containsKey("BeginFrom") ? LocalDate.parse(fields.get("BeginFrom"), formatter) : null);
    dateBeginTo.setValue(fields.containsKey("BeginTo") ? LocalDate.parse(fields.get("BeginTo"), formatter) : null);
    dateFinishFrom
        .setValue(fields.containsKey("FinishFrom") ? LocalDate.parse(fields.get("FinishFrom"), formatter) : null);
    dateFinishTo.setValue(fields.containsKey("FinishTo") ? LocalDate.parse(fields.get("FinishTo"), formatter) : null);
    cmbStatus.getSelectionModel().select(0);
    for (StatusFilterOption status : cmbStatus.getItems()) {
      if (status.getCode() == Integer.parseInt(fields.getOrDefault("Status", "0"))) {
        cmbStatus.setValue(status);
      }
    }
  }

  @FXML
  private void onFilterClear() {
    clearFilter(false);
  }

  private void lockUI() {
    lblTradesCount.setText(PENDING_MSG);
    wrapPane.setDisable(true);
    rootPane.getChildren().add(waitIndicator);
  }

  private void releaseUI() {
    rootPane.getChildren().remove(waitIndicator);
    wrapPane.setDisable(false);
  }
}
