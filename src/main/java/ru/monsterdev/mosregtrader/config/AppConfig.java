package ru.monsterdev.mosregtrader.config;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan({
    "ru.monsterdev.mosregtrader.config",
    "ru.monsterdev.mosregtrader.services",
    "ru.monsterdev.mosregtrader.repositories",
    "ru.monsterdev.mosregtrader.ui"})
@EnableTransactionManagement
@EnableScheduling
public class AppConfig {

  @Bean
  public CookieStore cookieStore() {
    return new BasicCookieStore();
  }

  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(5);
    threadPoolTaskScheduler.setThreadNamePrefix("TaskScheduler");
    return threadPoolTaskScheduler;
  }

}
