package ru.monsterdev.mosregtrader.services;

import java.util.List;
import ru.monsterdev.mosregtrader.domain.User;

public interface UserService {

  /**
   * Возвращает список всех пользователей, зарегистрированных в системе
   */
  List<User> findAll();

  /**
   * Регистрация нового пользователя в системе
   */
  User register(User newUser);

  /**
   * Устанавливает текущего пользователя
   */
  void setCurrentUser(User user);

  /**
   * Возвращает текущего пользователя
   */
  User getCurrentUser();

  /** Возвращает количество зарегистрированных пользователей */
  long getCount();
}
