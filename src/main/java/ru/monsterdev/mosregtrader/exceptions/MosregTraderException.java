package ru.monsterdev.mosregtrader.exceptions;

/**
 * Исключение, порождаемое приложением, как правило связанное с действиями пользователя
 */
public class MosregTraderException extends Exception {
    public MosregTraderException(String message) {
        super(message);
    }
}
