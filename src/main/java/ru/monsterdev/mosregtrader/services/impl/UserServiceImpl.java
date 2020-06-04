package ru.monsterdev.mosregtrader.services.impl;

import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.monsterdev.mosregtrader.domain.User;
import ru.monsterdev.mosregtrader.repositories.UserRepository;
import ru.monsterdev.mosregtrader.services.UserService;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserRepository userRepository;

  private User currentUser;

  @Override
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Override
  public long getCount() {
    return userRepository.count();
  }

  @Override
  public User register(User newUser) {
    newUser.setCreateDT(new Date());
    return userRepository.save(newUser);
  }

  @Override
  public void update() {
    userRepository.save(currentUser);
  }

  @Override
  public void setCurrentUser(User user) {
    this.currentUser = user;
  }

  @Override
  public User getCurrentUser() {
    return currentUser;
  }
}
