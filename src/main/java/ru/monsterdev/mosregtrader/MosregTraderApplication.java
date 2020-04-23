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
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.ui.UIComponent;
import ru.monsterdev.mosregtrader.utils.CipherUtil;
import ru.monsterdev.mosregtrader.utils.LicenseUtil;

@Slf4j
@SpringBootApplication
public class MosregTraderApplication extends AbstractApplication {

  private static MosregTraderApplication instance = null;
  private static Server db;

  @Autowired
  private UIComponent mainView;

  @Override
  public void start(Stage primaryStage) throws Exception {
    MosregTraderApplication.instance = this;
    primaryStage.setScene(new Scene(mainView.getView()));
    primaryStage.show();
    primaryStage.setOnCloseRequest(event -> {
      db.stop();
    });
  }

  public static MosregTraderApplication getInstance() {
    return instance;
  }

  public static URL getResource(String name) {
    return instance.getClass().getResource(name);
  }

  public static int work_thread_timeout;

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
        .longOpt("limit")
        .hasArg()
        .argName("COUNT")
        .build());
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(options, args);
      work_thread_timeout = Integer.parseInt(line.getOptionValue("sleep", "6000"));
      // нужно сгенерировать лицензионный ключ (максимальное кол-во зарегистрированных аккаунтов указывается в параметре limit)
      if (line.hasOption("license")) {
        Integer limit = Integer.parseInt(line.getOptionValue("limit", "1"));
        // формируем информацию о лицензии, с указанием кол-ва аккаунтов
        byte[] licenseInfo = String.format("usercount=%d", limit).getBytes(Charset.forName("UTF-8"));
        Checksum checksum = new CRC32();
        checksum.update(licenseInfo, 0, licenseInfo.length);
        // добавляем к информации о лицензии контрольную сумму
        String licenseData = String.format("usercount=%d;crc=%d", limit, checksum.getValue());
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
      log.trace("Starting with trades update interval {}", work_thread_timeout);
      launchApp(MosregTraderApplication.class, args);
    } catch (ParseException | IOException | SQLException | MosregTraderException ex) {
      if (db != null) {
        db.stop();
      }
      ex.printStackTrace();
    }
  }
}
