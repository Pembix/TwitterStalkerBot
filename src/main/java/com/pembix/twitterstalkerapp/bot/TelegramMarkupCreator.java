package com.pembix.twitterstalkerapp.bot;

import com.pembix.twitterstalkerapp.utils.Config;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class TelegramMarkupCreator {

    public static SendMessage createStartMenuMarkup(Long id) {
        SendMessage message = new SendMessage()
                .setChatId(id)
                .setText(Config.getString("MESSAGE_MARKUP_CHOOSE_OPTION"));
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        // Create a keyboard row
        KeyboardRow row = new KeyboardRow();
        // Set each button, you can also use KeyboardButton objects if you need something else than text
        row.add(Config.getString("BUTTON_SIGN_IN"));
        row.add(Config.getString("BUTTON_MANAGE_USERS"));
        keyboard.add(row);
        // Set the keyboard to the markup
        keyboardMarkup.setKeyboard(keyboard);
        // Add it to the message
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    public static SendMessage createSignedInMenuMarkup(Long id) {
        SendMessage message = new SendMessage()
                .setChatId(id)
                .setText(Config.getString("MESSAGE_MARKUP_CHOOSE_OPTION"));
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        // Set each button, you can also use KeyboardButton objects if you need something else than text
        row.add(Config.getString("BUTTON_REFRESH"));
        row.add(Config.getString("BUTTON_MANAGE_USERS"));
        // Add the first row to the keyboard
        keyboard.add(row);
        // Set the keyboard to the markup
        keyboardMarkup.setKeyboard(keyboard);
        // Add it to the message
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    public static SendMessage createManageUsersMarkup(Long id) {
        SendMessage message = new SendMessage()
                .setChatId(id)
                .setText(Config.getString("MESSAGE_MARKUP_CHOOSE_OPTION"));
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        // Set each button, you can also use KeyboardButton objects if you need something else than text
        row.add(Config.getString("BUTTON_TRACK_ALL"));
        row.add(Config.getString("BUTTON_TRACK_DELETED"));
        // Add the first row to the keyboard
        keyboard.add(row);
        // Create another keyboard row
        row = new KeyboardRow();
        // Set each button for the second line
        row.add(Config.getString("BUTTON_DELETE_USERS"));
        row.add(Config.getString("BUTTON_RETURN"));
        // Add the second row to the keyboard
        keyboard.add(row);
        // Set the keyboard to the markup
        keyboardMarkup.setKeyboard(keyboard);
        // Add it to the message
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    public static SendMessage createReplyKeyboardRemove(Long id) {
        SendMessage message = new SendMessage()
                .setChatId(id)
                .setText(Config.getString("MESSAGE_MARKUP_KEYBOARD_HIDDEN"));
        ReplyKeyboardRemove keyboardMarkup = new ReplyKeyboardRemove();
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }
}
