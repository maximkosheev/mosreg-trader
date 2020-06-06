package ru.monsterdev.mosregtrader.ui;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StringUtils;
import ru.monsterdev.mosregtrader.MosregTraderApplication;
import ru.monsterdev.mosregtrader.domain.FilterOption;
import ru.monsterdev.mosregtrader.domain.Trade;
import ru.monsterdev.mosregtrader.enums.FilterType;
import ru.monsterdev.mosregtrader.enums.TradeStatus;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.model.ProposalData;
import ru.monsterdev.mosregtrader.model.TradeFilter;
import ru.monsterdev.mosregtrader.model.dto.TradeInfoDto;
import ru.monsterdev.mosregtrader.services.DictionaryService;
import ru.monsterdev.mosregtrader.services.TradeService;
import ru.monsterdev.mosregtrader.services.scheduled.SubmitProposalService;
import ru.monsterdev.mosregtrader.services.scheduled.UpdateProposalsPrice;
import ru.monsterdev.mosregtrader.services.scheduled.UpdateTradesInfoService;
import ru.monsterdev.mosregtrader.tasks.UpdateTradesInfoTask;
import ru.monsterdev.mosregtrader.ui.control.WaitIndicator;
import ru.monsterdev.mosregtrader.ui.model.ProposalViewItem;

@Slf4j
public class MainController extends AbstractUIController {

  private static final String ERROR_COMMON_LOG_MSG = "Failed to complete operation due error %s : %s";
  private static final String PENDING_MSG = "Ожидайте, идет обновление...";
  private static final String IDLE_MSG = "Простой";
  private static final String ERROR_MSG = "Ошибка!";

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
  private ComboBox<TradeStatus> cmbStatus;
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
  private ApplicationContext applicationContext;
  @Autowired
  private DictionaryService dictionaryService;
  @Autowired
  private UIDispatcher uiDispatcher;
  private TradeFilter filterOptions = new TradeFilter();
  @Autowired
  private TradeService tradeService;
  @Autowired
  private ThreadPoolTaskScheduler threadPoolTaskScheduler;
  @Autowired
  private SubmitProposalService submitProposalService;
  @Autowired
  private UpdateTradesInfoService updateTradesInfoService;
  @Autowired
  private UpdateProposalsPrice updateProposalsPrice;

  @Override
  public void initialize() {
    cmbFilters.setOnAction(value -> {
      FilterOption filter = cmbFilters.getValue();
      if (filter != null) {
        clearFilter(true);
        setFilterFields(filter.getFields());
      }
    });
    cmbStatus.getItems().addAll(TradeStatus.allFromLocal());
    cmbStatus.getSelectionModel().select(0);

    cmbItemsPerPage.getItems().addAll(5, 10, 20, 50, 100);
    cmbItemsPerPage.setValue(10);
    cmbItemsPerPage.setOnAction(event -> doApplyFilter());

    webEngine = mainView.getEngine();
    webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == Worker.State.SUCCEEDED) { showTrades(); }
    });
    JSObject window = (JSObject) webEngine.executeScript("window");
    window.setMember("app", this);

    waitIndicator = new WaitIndicator();

    currentPageChangeListener = (observable, oldValue, newValue) -> doApplyFilter();
    pages.currentPageIndexProperty().addListener(currentPageChangeListener);
  }

  @Override
  public void bootstrap() {
    lblStatusText.setText(IDLE_MSG);
    cmbFilters.getItems().clear();
    cmbFilters.getItems().addAll(dictionaryService.findAllFilters(FilterType.PROPOSAL));

    webEngine.load(getClass().getResource("/ru/monsterdev/mosregtrader/ui/mainView.html").toExternalForm());

    threadPoolTaskScheduler.scheduleWithFixedDelay(submitProposalService, 60 * 1000);
    threadPoolTaskScheduler.scheduleWithFixedDelay(updateTradesInfoService, Date.from(LocalDateTime.now().plusMinutes(60)
        .atZone(ZoneId.systemDefault()).toInstant()), 60 * 60 * 1000);
    threadPoolTaskScheduler.scheduleWithFixedDelay(updateProposalsPrice, 1 * 60 * 1000);
  }

  private void prepareFilterOptions() {
    filterOptions.clear();

    if (!StringUtils.isEmpty(edtTradeNum.getText())) {
      filterOptions.setTradeNum(Long.parseLong(edtTradeNum.getText()));
    }
    if (!StringUtils.isEmpty(edtTradeName.getText())) {
      filterOptions.setTradeName(edtTradeName.getText());
    }
    if (dateBeginFrom.getValue() != null) {
      filterOptions.setBeginFrom(dateBeginFrom.getValue());
    }
    if (dateBeginTo.getValue() != null) {
      filterOptions.setBeginTo(dateBeginTo.getValue());
    }
    if (dateFinishFrom.getValue() != null) {
      filterOptions.setFinishFrom(dateFinishFrom.getValue());
    }
    if (dateFinishTo.getValue() != null) {
      filterOptions.setFinishTo(dateFinishTo.getValue());
    }
    if (cmbStatus.getValue() != null) {
      filterOptions.setStatus(cmbStatus.getValue());
    }
  }

  private void printProposals(List<Trade> trades) {
    List<ProposalViewItem> items = trades.stream().map(ProposalViewItem::new).collect(Collectors.toList());

    JSObject mainView = (JSObject) webEngine.executeScript("mainView");
    mainView.call("clearProposals");
    mainView.call("showProposals", items);
  }

  private void showTrades() {
    try {
      int countPerPage = cmbItemsPerPage.getValue();
      int startIndex = pages.getCurrentPageIndex() * countPerPage;
      // выборка закупок в соответствии с фильтром и текущей страницей
      List<Trade> filteredTrades = tradeService
          .findAll(filterOptions)
          .stream()
          .skip(startIndex).limit(startIndex + countPerPage)
          .collect(Collectors.toList());

      UpdateTradesInfoTask updateTradesInfoTask = applicationContext.getBean(UpdateTradesInfoTask.class, tradeService,
          filteredTrades);

      updateTradesInfoTask.setOnFailed(event -> {
        releaseUI();
        lblStatusText.setText(ERROR_MSG);
        UIController.showErrorMessage(updateTradesInfoTask.getException().getMessage());
      });

      updateTradesInfoTask.setOnSucceeded(event -> {
        releaseUI();
        printProposals(updateTradesInfoTask.getValue());
        lblStatusText.setText(IDLE_MSG);
        pages.setPageCount(Math.floorDiv(filteredTrades.size(), countPerPage) + 1);
        lblTradesCount.setText(String.valueOf(filteredTrades.size()));
      });

      lblStatusText.setText(PENDING_MSG);
      updateTradesInfoTask.start();
    } catch (Exception ex) {
      pages.setPageCount(1);
      lblTradesCount.setText(String.valueOf(0));
      lblStatusText.setText(ERROR_MSG);
      releaseUI();
    }
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
    lockUI();
    webEngine.reload();
  }

  @FXML
  private void onTradeNew() {
    Optional<List<TradeInfoDto>> tradeInfoList = uiDispatcher.showTradeFilterUI();
    if (!tradeInfoList.isPresent()) {
      return;
    }

    // проверка выбранных закупок уже в списке выбранных закупок
    if (tradeService.isContainAnyOf(tradeInfoList.get().stream().map(TradeInfoDto::getId).collect(Collectors.toList()))) {
      UIController.showErrorMessage("Некоторые из выбранных закупок уже находятся в обработке");
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
        doApplyFilter();
      }
    } catch (Throwable t) {
      log.error(String.format(ERROR_COMMON_LOG_MSG, t.getClass(), t.getMessage()));
      UIController.showErrorMessage(t.getMessage());
    }
  }

  @FXML
  private void onTradeEdit() {
    try {
      JSObject mainView = (JSObject) webEngine.executeScript("mainView");
      String result = mainView.call("getSelected").toString();
      if (result.isEmpty()) {
        throw new MosregTraderException("Выберете закупки, которые нужно редактировать, а затем повторите операцию");
      }

      String[] ids = result.split(",");
      if (ids.length < 1) {
        throw new MosregTraderException("Не выбрано ни одной закупки для удаления");
      }

      Optional<ProposalData> proposalData = uiDispatcher.showProposalDataUI();
      if (!proposalData.isPresent()) {
        return;
      }
      for (String id : ids) {
        Trade trade = tradeService.getTradeById(Long.parseLong(id));
        trade.setMinTradeVal(proposalData.get().getMinTradeVal());
        trade.setStartPrice(proposalData.get().getStartTradeVal());
        trade.setActivateTime(proposalData.get().getActivateTime());
        tradeService.saveTrade(trade);
      }
      doApplyFilter();
    } catch (Exception ex) {
      UIController.showErrorMessage(ex.getMessage());
    }
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
        tradeService.deleteTrade(Long.parseLong(id));
      }
      doApplyFilter();
    } catch (Throwable t) {
      UIController.showErrorMessage(t.getMessage());
    }
  }

  public void onOpenTradePage(Integer tradeId) {
    if (tradeId != null) {
      HostServicesFactory.getInstance(MosregTraderApplication.getInstance())
          .showDocument("https://market.mosreg.ru/Trade/ViewTrade/" + tradeId);
    } else {
      UIController.showErrorMessage("Ошибка при попытке открыть окно браузера по данной закупке");
    }
  }

  @FXML
  private void onProfile() {
    uiDispatcher.showEditProfileUI();
  }

  @FXML
  private void onAbout() {
    //
  }

  private void doApplyFilter() {
    lockUI();
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
    Optional<String> optFilterName = dlg.showAndWait();
    if (!optFilterName.isPresent()) {
      UIController.showErrorMessage("Вы должны задать имя фильтра");
      return;
    }
    String filterName = optFilterName.get();
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
    for (TradeStatus status : cmbStatus.getItems()) {
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
