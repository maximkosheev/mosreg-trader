package ru.monsterdev.mosregtrader.utils.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import ru.monsterdev.mosregtrader.utils.StringUtil;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

  public LocalDateTimeDeserializer() {
    super(LocalDateTime.class);
  }

  @Override
  public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    return StringUtil.toLocalDateTime(p.readValueAs(String.class));
  }
}
