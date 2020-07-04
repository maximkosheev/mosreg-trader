package ru.monsterdev.mosregtrader.utils;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;

@Slf4j
public class LicenseUtil {
  @Getter
  private static int accountsLimit = 0;
  @Getter
  private static LocalDate untilLimit = LocalDate.MIN;

  private static boolean validateChecksum(String data, long crc32) {
    byte[] bytes = data.getBytes(Charset.forName("UTF-8"));
    Checksum checksum = new CRC32();
    checksum.update(bytes, 0, bytes.length);
    long value = checksum.getValue();
    return value == crc32;
  }
  /**
   * Выполняет загрузку информации о лицензии
   *
   * @param licenseData строка, содержащая данные о лицензии
   */
  public static void load(@NonNull String licenseData) throws MosregTraderException {
    try {
      Pattern dataPattern = Pattern.compile("^usercount=(\\d+);until=(\\d{4}-\\d{2}-\\d{2});crc=(\\d+)$");
      Matcher matcher = dataPattern.matcher(licenseData);
      if (!matcher.find())
        throw new MosregTraderException("Ошибка чтения лицензионной информации, возможно файл лицензии поврежден");
      int count = Integer.parseInt(matcher.group(1));
      String until = matcher.group(2);
      long crc = Long.parseLong(matcher.group(3));
      if (!validateChecksum(String.format("usercount=%d;until=%s", count, until), crc))
        throw new MosregTraderException("Ошибка чтения лицензионной информации, возможно файл лицензии поврежден");
      LicenseUtil.accountsLimit = count;
      LicenseUtil.untilLimit = LocalDate.parse(until);
    } catch (Exception ex) {
      log.error("Ошибка чтения лицензионной информации: ", ex);
      throw new MosregTraderException("Ошибка чтения лицензионной информации, возможно файл лицензии поврежден");
    }
  }

  /**
   * Выполняет проверку ограничения лицензии на кол-во зарегистрированных пользователей
   *
   * @param usersCount текущее число пользователей
   * @return истина, если ограничение не нарушено и ложь в противном случае
   */
  public static boolean checkAccountsLimit(long usersCount) {
    return LicenseUtil.accountsLimit >= usersCount;
  }

  /**
   * Выполняет проверку ограничения лицензии на срок действия
   * @param date
   * @return
   */
  public static boolean checkDateLimit(@NonNull LocalDate date) {
    return untilLimit.isAfter(date);
  }
}
