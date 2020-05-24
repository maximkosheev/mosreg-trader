package ru.monsterdev.mosregtrader.tasks;

import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.http.Session;
import ru.monsterdev.mosregtrader.http.TraderResponse;
import ru.monsterdev.mosregtrader.http.requests.GetAccountPageRequest;
import ru.monsterdev.mosregtrader.http.requests.GetLoginPageRequest;
import ru.monsterdev.mosregtrader.http.requests.LoginRequest;
import ru.monsterdev.mosregtrader.model.CertificateInfo;
import ru.monsterdev.mosregtrader.services.CryptoService;
import ru.monsterdev.mosregtrader.services.HttpService;
import ru.monsterdev.mosregtrader.utils.StringUtil;

@Slf4j
@Component
public class LoginTask extends Task<Boolean> implements TraderTask {

  private static final String LOGIN_ERROR_1 = "Ошибка при запросе страницы входа";
  private static final String LOGIN_ERROR_2 = "Ошибка авторизации";
  private static final String LOGIN_ERROR_3 = "Ошибка авторизации: ЭЦП не настроена";
  private static final String LOGIN_ERROR_4 = "Ошибка получения страницы аккаунта";

  private CertificateInfo certInfo;

  @Autowired
  private HttpService httpService;

  @Autowired
  private CryptoService cryptoService;

  public void setCertInfo(CertificateInfo certInfo) {
    this.certInfo = certInfo;
  }

  @Override
  protected Boolean call() throws Exception {
    try {
      log.trace("Trying to authorize");
      // 1. Получить страницу входа
      String loginPageContent = Objects
          .requireNonNull(httpService.sendRequest(new GetLoginPageRequest()).getEntity(), LOGIN_ERROR_1);
      /* 2. Из полученной страницы входа нужно вычленить вот такую строку window.dateDateForSign = "MTIvMTIvMjAxOCAxODoxMjowOA==" */
      String regexp = "window.dateDateForSign\\s?=\\s?\"(\\w+==)\"";
      Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(loginPageContent);
      // 3. Из полученной строки выбираем данные, которые нужно подписать
      String dataForSign = matcher.find() ? matcher.group(1) : null;
      // 4. Подписываем данные ЭЦП, относящуюся к выбранному пользователю
      byte[] signature = cryptoService.signData(certInfo, dataForSign);
      // 5. Отправка данных формы входа обратно на сервер
      // Если авторизация прошла успешно, площадка перенаправляет нас на тот URL, указанный как returnUrl.
      if (signature == null) {
        throw new MosregTraderException(LOGIN_ERROR_3);
      }
      if (httpService.sendRequest(new LoginRequest("/", certInfo.getHash(), new String(signature))).getCode()
          != SC_MOVED_TEMPORARILY) {
        throw new MosregTraderException(LOGIN_ERROR_2);
      }
      // 6. А мы пойдем на страницу аккаунта текущего пользователя, чтобы получить код авторизации
      TraderResponse response = httpService.sendRequest(new GetAccountPageRequest());
      if (response.getCode() != SC_OK) {
        throw new MosregTraderException(LOGIN_ERROR_4);
      }
      String authCode = StringUtil.parseForAuthCode(response.getEntity());
      Session.getInstance().setProperty("authCode", authCode);
      log.trace("Authorization is finished");
      return true;
    } catch (Exception ex) {
      log.error("", ex);
      throw ex;
    }
  }

  @Override
  public void start() {
    Thread thread = new Thread(this);
    thread.start();
  }
}
