package com.pembix.twitterstalkerapp.model.enums;

public enum TelegramUserAction {

    DELETE,
    TRACK_ALL,
    TRACK_DELETED,
    AUTH;

    public static TelegramUserAction from(String type) {
        for (TelegramUserAction telegramUserAction : values()) {
            if (telegramUserAction.name().equals(type)) {
                return telegramUserAction;
            }
        }

        return null;
    }

}
