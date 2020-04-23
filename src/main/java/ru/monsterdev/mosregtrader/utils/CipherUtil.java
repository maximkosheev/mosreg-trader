package ru.monsterdev.mosregtrader.utils;

import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;

@Slf4j
public class CipherUtil {
  private static final String secretKey = "4ATxIA4lDqs=";
  private static final byte[] iv = { 11, 22, 33, 44, 99, 88, 77, 66 };

  public static byte[] encrypt(byte[] data) throws MosregTraderException {
    try {
      byte[] decodeKey = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));
      SecretKey key = new SecretKeySpec(decodeKey, 0, decodeKey.length, "DES");
      AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
      Cipher encryptCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
      encryptCipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
      return encryptCipher.doFinal(data);
    } catch (Exception ex) {
      log.error("Ошибка шифрования: ", ex);
      throw new MosregTraderException("Ошибка шифрования");
    }
  }

  public static byte[] decrypt(byte[] data) throws MosregTraderException {
    try {
      byte[] decodeKey = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));
      SecretKey key = new SecretKeySpec(decodeKey, 0, decodeKey.length, "DES");
      AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
      Cipher encryptCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
      encryptCipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
      return encryptCipher.doFinal(data);
    } catch (Exception ex) {
      log.error("Ошибка расшифровки: ", ex);
      throw new MosregTraderException("Ошибка расшифровки");
    }
  }
}
