package com.pembix.twitterstalkerapp.model.enums;

public enum TelegramTwitterRelationType {

    AUTO_DELETED,
    MANUAL_DELETED,
    MANUAL_ALL;

    public static TelegramTwitterRelationType from(String type) {
        for (TelegramTwitterRelationType telegramTwitterRelationType : values()) {
            if (type.equals(telegramTwitterRelationType.name())) {
                return telegramTwitterRelationType;
            }
        }

        return null;
    }
}
