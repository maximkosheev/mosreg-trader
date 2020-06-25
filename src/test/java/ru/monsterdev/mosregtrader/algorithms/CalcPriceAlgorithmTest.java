package ru.monsterdev.mosregtrader.algorithms;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ru.monsterdev.mosregtrader.exceptions.MosregTraderException;
import ru.monsterdev.mosregtrader.model.dto.ProductDto;

@Ignore
public class CalcPriceAlgorithmTest {

  private CalcPriceAlgorithm calcAlgorithm;

  @Before
  public void init() {
    calcAlgorithm = new CalcPriceAlgorithm();
  }

  @Test
  public void doCalc() throws MosregTraderException, InterruptedException {
    Set<ProductDto> products = new HashSet<>();
    BigDecimal requiredPrice = new BigDecimal("39600.00");
    ProductDto product = new ProductDto();
    product.setQuantity(new BigDecimal(1L));
    product.setPrice(new BigDecimal("100000.00"));
    products.add(product);
    long before = System.currentTimeMillis();
    calcAlgorithm.doCalc(products, requiredPrice);
    System.out.println(product.getPrice());
    System.out.println(String.format("After: %02.2f", (System.currentTimeMillis() - before) / 1000.0));
  }
}