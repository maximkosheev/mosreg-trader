package ru.monsterdev.mosregtrader;

import javafx.application.Application;
import javafx.application.Platform;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class AbstractApplication extends Application {
  private static String[] args;

  private ConfigurableApplicationContext applicationContext;

  @Override
  public void init() throws Exception {
    Platform.runLater(() -> {
      applicationContext = SpringApplication.run(getClass(), args);
      applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    });
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    applicationContext.close();
  }

  static void launchApp(Class<? extends AbstractApplication> clazz, String[] args) {
    AbstractApplication.args = args;
    Application.launch(clazz, args);
  }
}
