package com.pembix.twitterstalkerapp.utils;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

/**
 * Functional interface which is used to transfer and store {@link org.telegram.telegrambots.meta.bots.AbsSender#execute}
 *
 * @param <T>
 * @param <R>
 */
@FunctionalInterface
public interface Chat<T extends BotApiMethod<R>, R extends Serializable> {
    R send(T t) throws TelegramApiException;
}
