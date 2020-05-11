package ru.monsterdev.mosregtrader.ui.control;

import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import ru.monsterdev.mosregtrader.ui.model.TradeViewItem;

public class MultilineCellFactory implements Callback<TableColumn<TradeViewItem, String>, TableCell<TradeViewItem, String>> {
    @Override
    public TableCell<TradeViewItem, String> call(TableColumn<TradeViewItem, String> param) {
        final TableCell<TradeViewItem, String> cell = new TableCell<TradeViewItem, String>() {
            private Text text = null;
            private static final int PADDING = 10;

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setPrefHeight(Control.USE_COMPUTED_SIZE);
                if (!empty) {
                    text = new Text(item);
                    text.setTextAlignment(TextAlignment.CENTER);
                    text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(PADDING));
                    setGraphic(text);
                }
            }
        };
        return cell;
    }
}
