package ru.monsterdev.mosregtrader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ru.monsterdev.mosregtrader.events.ShowUIEvent;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.ui.UIComponent;
import ru.monsterdev.mosregtrader.ui.UIDispatcher;
import ru.monsterdev.mosregtrader.utils.CipherUtil;
import ru.monsterdev.mosregtrader.utils.LicenseUtil;

@Slf4j
@SpringBootApplication
public class MosregTraderApplication extends AbstractApplication {

  private static Server db;
  private static MosregTraderApplication instance;

  @Autowired
  private UIDispatcher uiDispatcher;

  public static MosregTraderApplication getInstance() {
    return instance;
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    instance = this;
    uiDispatcher.setPrimaryStage(primaryStage);
    primaryStage.setOnCloseRequest(event -> {
      db.stop();
    });
    uiDispatcher.showLoginUI();
  }

  public static void main(String[] args) {
    System.setProperty("com.sun.security.enableCRLDP", "true");
    System.setProperty("com.ibm.security.enableCRLDP", "true");

    Options options = new Options();
    options.addOption(Option.builder()
        .longOpt("sleep")
        .hasArg()
        .argName("SLEEP")
        .build());
    options.addOption(Option.builder()
        .longOpt("license")
        .build());
    options.addOption(Option.builder()
        .longOpt("accountsLimit")
        .hasArg()
        .argName("COUNT")
        .build());
    options.addOption(Option.builder()
        .longOpt("untilLimit")
        .hasArg()
        .argName("UNTIL")
        .build());
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(options, args);
      // нужно сгенерировать лицензионный ключ (максимальное кол-во зарегистрированных аккаунтов указывается в параметре limit)
      if (line.hasOption("license")) {
        Integer limit = Integer.parseInt(line.getOptionValue("accountsLimit", "1"));
        String limitDate = line.getOptionValue("untilLimit", "1970-01-01");
        // формируем информацию о лицензии, с указанием кол-ва аккаунтов
        byte[] licenseInfo = String.format("usercount=%d;until=%s", limit, limitDate).getBytes(Charset.forName("UTF-8"));
        Checksum checksum = new CRC32();
        checksum.update(licenseInfo, 0, licenseInfo.length);
        // добавляем к информации о лицензии контрольную сумму
        String licenseData = String.format("usercount=%d;until=%s;crc=%d", limit, limitDate, checksum.getValue());
        // формируем файл лицензии при этом зашифровываем информацию о лицензии
        FileOutputStream fos = new FileOutputStream("license.key");
        fos.write(CipherUtil.encrypt(licenseData.getBytes(Charset.forName("UTF-8"))));
        fos.close();
      }
      log.trace("Reading license data from file license.key");
      Path licenseFilePath = Paths.get(System.getenv("APP_HOME")).resolve("bin").resolve("license.key");
      String licenseData = new String(CipherUtil.decrypt(Files.readAllBytes(licenseFilePath)), StandardCharsets.UTF_8);
      LicenseUtil.load(licenseData);
      log.trace("Starting database...");
      db = Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "9092", "-trace").start();
      log.trace("Database started at: " + db.getURL());
      log.trace("Stating application");
      launchApp(MosregTraderApplication.class, args);
    } catch (ParseException | IOException | SQLException | MosregTraderException ex) {
      if (db != null) {
        db.stop();
      }
      ex.printStackTrace();
    }
  }
}
