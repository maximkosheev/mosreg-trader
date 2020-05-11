package ru.monsterdev.mosregtrader.utils.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateSerializers {

  protected abstract static class DateBaseSerializer<T> extends StdSerializer<T> implements ContextualSerializer {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd'T'hh:mm:ss";

    protected DateTimeFormatter df;
    protected String pattern;

    protected DateBaseSerializer(Class<T> type) {
      super(type);
      this.pattern = DEFAULT_PATTERN;
      this.df = DateTimeFormatter.ofPattern(this.pattern);
    }

    protected DateBaseSerializer(Class<T> type, DateTimeFormatter dtFormatter, String pattern) {
      this(type);
      this.pattern = pattern;
      this.df = dtFormatter;
    }

    protected abstract DateBaseSerializer<T> withFormat(DateTimeFormatter dtFormatter, String pattern);

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
        throws JsonMappingException {
      return null;
    }
  }

  public static class LocalDateSerializer extends DateBaseSerializer<LocalDate> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd";

    public LocalDateSerializer() {
      super(LocalDate.class);
      this.pattern = DEFAULT_PATTERN;
      this.df = DateTimeFormatter.ofPattern(this.pattern);
    }

    public LocalDateSerializer(DateTimeFormatter dtFormatter, String pattern) {
      super(LocalDate.class, dtFormatter, pattern);
    }

    @Override
    protected DateBaseSerializer<LocalDate> withFormat(DateTimeFormatter dtFormatter, String pattern) {
      return new LocalDateSerializer(dtFormatter, pattern);
    }

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeString(value.format(df));
    }
  }

  public static class LocalDateTimeSerializer extends DateBaseSerializer<LocalDateTime> {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd'T'hh:mm:ss";

    public LocalDateTimeSerializer() {
      super(LocalDateTime.class);
      this.pattern = DEFAULT_PATTERN;
      this.df = DateTimeFormatter.ofPattern(this.pattern);
    }

    public LocalDateTimeSerializer(DateTimeFormatter dtFormatter, String pattern) {
      super(LocalDateTime.class, dtFormatter, pattern);
    }

    @Override
    protected DateBaseSerializer<LocalDateTime> withFormat(DateTimeFormatter dtFormatter, String pattern) {
      return new LocalDateTimeSerializer(dtFormatter, pattern);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeString(value.format(df));
    }
  }
}
