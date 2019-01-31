package com.pembix.twitterstalkerapp.model.enums;

public enum TelegramUserStatus {

    ACTIVE,
    INACTIVE;

    public static TelegramUserStatus from(String type) {
        for (TelegramUserStatus telegramUserStatus : values()) {
            if (telegramUserStatus.name().equals(type)) {
                return telegramUserStatus;
            }
        }

        return null;
    }
}
