package ru.monsterdev.mosregtrader.ui;

import javafx.scene.Parent;
import lombok.Data;

@Data
public class UIComponent {
  private final Parent view;
  private final UIController controller;
}
