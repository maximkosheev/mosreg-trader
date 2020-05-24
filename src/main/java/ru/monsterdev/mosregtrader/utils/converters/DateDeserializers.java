package ru.monsterdev.mosregtrader.utils.converters;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateDeserializers {

  protected abstract static class DateBaseDeserializer<T> extends StdDeserializer<T> implements ContextualDeserializer {

    private final static String DEFAULT_PATTERN = "yyyy-MM-dd'T'hh:mm:ss";

    String pattern;
    DateTimeFormatter df;

    DateBaseDeserializer(Class<?> vc) {
      super(vc);
      pattern = DEFAULT_PATTERN;
      df = DateTimeFormatter.ofPattern(pattern);
    }

    DateBaseDeserializer(DateBaseDeserializer<T> base, DateTimeFormatter dtFormatter, String pattern) {
      super(base._valueClass);
      this.pattern = pattern;
      this.df = dtFormatter;
    }

    protected abstract DateBaseDeserializer<T> withFormat(DateTimeFormatter dtFormatter, String pattern);

    /* Обрезаем строку, которая содержит дату-время, по длине маски. Для чего мы это делаем?
     * От сервера бывает приходит дата-время в формате yyyy-MM-dd'T'HH:mm:ss.SSS'Z', бывает yyyy-MM-dd'T'HH:mm:ss'Z',
     * бывает yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z', может быть еще какой-то, на который я не натыкался. Объеденить все
     * эти форматы - оказалось проблемой. Вот поэтому нужно это обрезания, которое осталяет только ту часть строки,
     * которая описана маской (да, я не предусматривал всякие извращенский маски, типа как в PHP)
     */
    protected String prepareString(String value, String mask) {
      return (value != null && value.length() >= mask.length()) ? value.substring(0, mask.length()) : value;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
        throws JsonMappingException {
      final JsonFormat.Value format = findFormatOverrides(ctxt, property, handledType());

      if (format != null) {
        pattern = format.hasPattern() ? format.getPattern() : DEFAULT_PATTERN;
        df = DateTimeFormatter.ofPattern(pattern);
        return withFormat(df, pattern);
      }
      return this;
    }
  }

  public static class LocalDateDeserializer extends DateBaseDeserializer<LocalDate> {

    private final static String DEFAULT_PATTERN = "yyyy-MM-dd";

    public LocalDateDeserializer() {
      super(LocalDate.class);
      this.pattern = DEFAULT_PATTERN;
      this.df = DateTimeFormatter.ofPattern(this.pattern);
    }

    public LocalDateDeserializer(Class<?> vc) {
      super(vc);
      this.pattern = DEFAULT_PATTERN;
      this.df = DateTimeFormatter.ofPattern(this.pattern);
    }

    LocalDateDeserializer(LocalDateDeserializer src, DateTimeFormatter dtFormatter, String pattern) {
      super(src, dtFormatter, pattern);
    }

    @Override
    protected DateBaseDeserializer<LocalDate> withFormat(DateTimeFormatter dtFormatter, String pattern) {
      return new LocalDateDeserializer(this, dtFormatter, pattern);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      String value = p.getText();
      try {
        return value != null ? LocalDate.parse(prepareString(value, pattern)) : null;
      } catch (Exception ex) {
        throw new JsonParseException(p, value, ex);
      }
    }
  }

  public static class LocalDateTimeDeserializer extends DateBaseDeserializer<LocalDateTime> {

    private final static String DEFAULT_PATTERN = "yyyy-MM-dd'T'hh:mm:ss";

    public LocalDateTimeDeserializer() {
      super(LocalDateTime.class);
      this.pattern = DEFAULT_PATTERN;
      this.df = DateTimeFormatter.ofPattern(this.pattern);
    }

    public LocalDateTimeDeserializer(Class<?> vc) {
      super(vc);
      this.pattern = DEFAULT_PATTERN;
      this.df = DateTimeFormatter.ofPattern(this.pattern);
    }

    LocalDateTimeDeserializer(LocalDateTimeDeserializer src, DateTimeFormatter dtFormatter, String pattern) {
      super(src, dtFormatter, pattern);
    }

    @Override
    protected DateBaseDeserializer<LocalDateTime> withFormat(DateTimeFormatter dtFormatter, String pattern) {
      return new LocalDateTimeDeserializer(this, dtFormatter, pattern);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      String value = prepareString(p.getText(), "yyyy-MM-ddThh:mm:ss");
      try {
        return value != null ? ZonedDateTime.of(LocalDateTime.parse(value), ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() : null;
      } catch (Exception ex) {
        throw new JsonParseException(p, value, ex);
      }
    }
  }
}
