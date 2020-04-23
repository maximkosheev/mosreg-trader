package ru.monsterdev.mosregtrader.domain;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import ru.monsterdev.mosregtrader.enums.FilterType;

@Data
@Entity
@Table(name = "filters")
public class FilterOption {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "name")
  private String name;

  @Column(name = "type")
  @Enumerated(EnumType.ORDINAL)
  private FilterType type;

  @Column(name = "options")
  private String options;

  public Map<String, String> getFields() {
    Map<String, String> fields = new HashMap<>();
    String[] pairs = options.split(";");
    for (String pair : pairs) {
      String[] keyvalue = pair.split("=");
      fields.put(keyvalue[0], keyvalue[1]);
    }
    return fields;
  }

  public void setFields(Map<String, String> fields) {
    options = "";
    fields.forEach((key, value) -> {
      options += (key + "=" + value + ";");
    });
    System.out.println(options);
  }

  @Override
  public String toString() {
    return name;
  }

    /*
    public static String prepareString(String string)  {
        return StringUtil.removeAll(StringUtil.removeWhitespaces(string), ";", "=");
    }
    */
}
