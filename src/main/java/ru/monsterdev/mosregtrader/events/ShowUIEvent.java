package ru.monsterdev.mosregtrader.events;

import javafx.event.Event;
import javafx.event.EventType;
import ru.monsterdev.mosregtrader.enums.WindowType;

public class ShowUIEvent extends Event {

  private WindowType windowType;

  public static final EventType<ShowUIEvent> SHOW_UI = new EventType<>(Event.ANY, "SHOW_UI");

  public static final ShowUIEvent SHOW_LOGIN_UI = new ShowUIEvent(SHOW_UI, WindowType.LOGIN);
  public static final ShowUIEvent SHOW_REGISTER_UI = new ShowUIEvent(SHOW_UI, WindowType.REGISTER);
  public static final ShowUIEvent SHOW_MAIN_UI = new ShowUIEvent(SHOW_UI, WindowType.MAIN);

  private ShowUIEvent(EventType<ShowUIEvent> eventType, WindowType windowType) {
    super(eventType);
    this.windowType = windowType;
  }

  public WindowType getWindowType() {
    return windowType;
  }
}
