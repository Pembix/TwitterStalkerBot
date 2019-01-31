package com.pembix.twitterstalkerapp;

import com.pembix.twitterstalkerapp.bot.CustomTelegramBot;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Launcher {

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Launcher.class);
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(new CustomTelegramBot());
        } catch (TelegramApiException e) {
            logger.error(e);
        }
    }
}
