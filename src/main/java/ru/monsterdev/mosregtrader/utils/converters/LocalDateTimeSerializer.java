package ru.monsterdev.mosregtrader.utils.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.LocalDateTime;
import ru.monsterdev.mosregtrader.utils.StringUtil;

public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

  public LocalDateTimeSerializer() {
    super(LocalDateTime.class);
  }

  @Override
  public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(StringUtil.fromLocalDate(value));
  }
}
