package ru.monsterdev.mosregtrader.core;

import java.util.Observer;

public interface IObservable {

  void addObserver(Observer o);

  void deleteObserver(Observer o);

  void notifyObservers();

  void notifyObservers(Object arg);

  void deleteObservers();

  boolean hasChanged();

  int countObservers();

}
