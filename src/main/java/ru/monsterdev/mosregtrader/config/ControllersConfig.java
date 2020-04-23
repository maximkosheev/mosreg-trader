package ru.monsterdev.mosregtrader.config;

import java.io.IOException;
import java.io.InputStream;
import javafx.fxml.FXMLLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.monsterdev.mosregtrader.ui.UIComponent;
import ru.monsterdev.mosregtrader.ui.MainController;

@Slf4j
@Configuration
public class ControllersConfig {

  private UIComponent loadComponent(String url) {
    try (InputStream inputStream = getClass().getResourceAsStream(url)) {
      FXMLLoader loader = new FXMLLoader();
      loader.load(inputStream);
      return new UIComponent(loader.getRoot(), loader.getController());
    } catch (IOException ex) {
      log.error("Не могу загрузить ресурс {}: ошибка: ", url, ex);
      return null;
    }
  }

  @Bean(name = "mainView")
  public UIComponent getMainView() {
    return loadComponent("/ru/monsterdev/mosregtrader/ui/main.fxml");
  }

  @Bean
  public MainController getMainController() {
    return (MainController)getMainView().getController();
  }

}
