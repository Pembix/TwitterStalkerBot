package com.pembix.twitterstalkerapp.bot;

import com.pembix.twitterstalkerapp.dao.TelegramUserDao;
import com.pembix.twitterstalkerapp.model.TelegramTwitterRelation;
import com.pembix.twitterstalkerapp.model.TelegramUser;
import com.pembix.twitterstalkerapp.model.enums.TelegramTwitterRelationType;
import com.pembix.twitterstalkerapp.model.enums.TelegramUserAction;
import com.pembix.twitterstalkerapp.model.enums.TelegramUserStatus;
import com.pembix.twitterstalkerapp.utils.Config;
import com.pembix.twitterstalkerapp.utils.TwitterStreamHelper;
import com.pembix.twitterstalkerapp.utils.TwitterUtils;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import twitter4j.auth.AccessToken;

import java.util.*;

import static com.pembix.twitterstalkerapp.bot.TelegramMarkupCreator.*;

public class CustomTelegramBot extends TelegramLongPollingBot {
    private Logger logger = Logger.getLogger(CustomTelegramBot.class);

    private TelegramUserDao telegramUserDao = TelegramUserDao.implementation();
    private TwitterStreamHelper twitterStreamHelper = new TwitterStreamHelper(this::execute);
    private TwitterUtils twitterUtils = new TwitterUtils();

    public CustomTelegramBot() {
        twitterStreamHelper.startStream();
        new Thread(twitterStreamHelper).start();
    }

    void setTelegramUserDao(TelegramUserDao telegramUserDao) {
        this.telegramUserDao = telegramUserDao;
    }

    void setTwitterStreamHelper(TwitterStreamHelper twitterStreamHelper) {
        this.twitterStreamHelper = twitterStreamHelper;
    }

    void setTwitterUtils(TwitterUtils twitterUtils) {
        this.twitterUtils = twitterUtils;
    }

    @Override
    public void onUpdateReceived(Update update) {
        TelegramUser telegramUser = telegramUserDao.get(update.getMessage().getChatId());
        if (update.hasMessage()) {
            String text = update.getMessage().getText();
            if (telegramUser == null || text.equals(Config.getString("COMMAND_START"))) {
                commandStart(update, telegramUser);
            } else if (text.equals(Config.getString("COMMAND_KEYBOARD"))) {
                commandKeyboard(telegramUser);
            } else if (text.equals(Config.getString("COMMAND_STOP"))) {
                commandStop(telegramUser);
            } else if (text.equals(Config.getString("COMMAND_DELETE"))) {
                commandDelete(telegramUser);
            } else if (text.equals(Config.getString("COMMAND_HELP"))) {
                commandHelp(telegramUser);
            } else if (text.equals(Config.getString("BUTTON_SIGN_IN"))) {
                buttonSignIn(telegramUser);
            } else if (text.equals(Config.getString("BUTTON_MANAGE_USERS"))) {
                buttonManageUsers(telegramUser);
            } else if (text.equals(Config.getString("BUTTON_REFRESH"))) {
                buttonRefresh(telegramUser);
            } else if (text.equals(Config.getString("BUTTON_RETURN"))) {
                buttonReturn(telegramUser);
            } else if (text.equals(Config.getString("BUTTON_DELETE_USERS"))) {
                buttonDeleteUsers(telegramUser);
            } else if (text.equals(Config.getString("BUTTON_TRACK_ALL"))) {
                buttonTrackAll(telegramUser);
            } else if (text.equals(Config.getString("BUTTON_TRACK_DELETED"))) {
                buttonTrackDeleted(telegramUser);
            } else if (TelegramUserAction.DELETE.equals(telegramUser.getAction())) {
                deleteTwitterUsers(telegramUser, text);
            } else if (TelegramUserAction.TRACK_ALL.equals(telegramUser.getAction())) {
                trackAllOrDeletedTweets(telegramUser, text);
            } else if (TelegramUserAction.TRACK_DELETED.equals(telegramUserDao.get(update.getMessage().getChatId()).getAction())) {
                trackAllOrDeletedTweets(telegramUser, text);
            } else if (TelegramUserAction.AUTH.equals(telegramUser.getAction())) {
                actionAuth(telegramUser, text);
            } else {
                sendToChat(telegramUser.getId(), Config.getString("MESSAGE_UPDATE_ERROR"));
            }
        }
    }

    private void commandStart(Update update, TelegramUser telegramUser) {
        if (telegramUser == null) {
            long telegramUserId = update.getMessage().getChatId();
            telegramUserDao.save(new TelegramUser(telegramUserId));
            sendToChat(telegramUserId, Config.getString("MESSAGE_START"));
            sendToChat(createStartMenuMarkup(telegramUserId));
        } else if (!telegramUser.isTwitterAuthorized()) {
            if (TelegramUserStatus.INACTIVE.equals(telegramUser.getStatus())) {
                telegramUser.setStatus(TelegramUserStatus.ACTIVE);
                telegramUserDao.update(telegramUser);
                twitterStreamHelper.queueRestartStream();
            }
            sendToChat(telegramUser.getId(), Config.getString("MESSAGE_START"));
            sendToChat(createStartMenuMarkup(telegramUser.getId()));
        } else {
            if (TelegramUserStatus.INACTIVE.equals(telegramUser.getStatus())) {
                telegramUser.setStatus(TelegramUserStatus.ACTIVE);
                telegramUserDao.update(telegramUser);
                twitterStreamHelper.queueRestartStream();
            }
            sendToChat(createSignedInMenuMarkup(telegramUser.getId()));
        }
    }

    private void commandKeyboard(TelegramUser telegramUser) {
        if (!telegramUser.isTwitterAuthorized()) {
            sendToChat(createStartMenuMarkup(telegramUser.getId()));
        } else {
            sendToChat(createSignedInMenuMarkup(telegramUser.getId()));
        }
    }

    private void commandStop(TelegramUser telegramUser) {
        telegramUser.setStatus(TelegramUserStatus.INACTIVE);
        telegramUserDao.update(telegramUser);
        twitterStreamHelper.queueRestartStream();
        sendToChat(telegramUser.getId(), Config.getString("MESSAGE_STOP"));
        sendToChat(createReplyKeyboardRemove(telegramUser.getId()));
    }

    private void commandDelete(TelegramUser telegramUser) {
        telegramUserDao.delete(telegramUser.getId());
        twitterStreamHelper.queueRestartStream();
        SendMessage message = new SendMessage()
                .setChatId(telegramUser.getId())
                .setText(Config.getString("MESSAGE_DELETE")).setParseMode("HTML");
        sendToChat(message);
        sendToChat(createReplyKeyboardRemove(telegramUser.getId()));
    }

    private void buttonSignIn(TelegramUser telegramUser) {
        if (!telegramUser.isTwitterAuthorized()) {
            telegramUser.setAction(TelegramUserAction.AUTH);
            telegramUserDao.update(telegramUser);
            sendToChat(telegramUser.getId(), Config.getString("MESSAGE_BEGIN_REG"));
            sendToChat(telegramUser.getId(), twitterUtils.getAuthorizationURL(telegramUser.getId()));
        } else {
            sendToChat(telegramUser.getId(), Config.getString("MESSAGE_BEGIN_REG_ERROR"));
        }
    }

    private void buttonManageUsers(TelegramUser telegramUser) {
        telegramUser.setAction(null);
        telegramUserDao.update(telegramUser);
        sendToChat(createManageUsersMarkup(telegramUser.getId()));
    }

    private void buttonRefresh(TelegramUser telegramUser) {
        int followingAmount = getTwitterFollowingList(telegramUser);
        //followingAmount == -1 if user reached rate limit per making calls (15 calls per 15 minutes)
        if (followingAmount == -1 ) {
            sendToChat(telegramUser.getId(), Config.getString("MESSAGE_API_LIMIT"));
        } else {
            telegramUserDao.update(telegramUser);
            twitterStreamHelper.queueRestartStream();
            sendToChat(telegramUser.getId(), String.format(Config.getString("MESSAGE_REFRESH_USERS"), followingAmount));
        }
    }

    private void buttonReturn(TelegramUser telegramUser) {
        if (!telegramUser.isTwitterAuthorized()) {
            sendToChat(createStartMenuMarkup(telegramUser.getId()));
        } else {
            sendToChat(createSignedInMenuMarkup(telegramUser.getId()));
        }
    }

    private void commandHelp(TelegramUser telegramUser) {
        SendMessage message = new SendMessage()
                .setChatId(telegramUser.getId())
                .setText(Config.getString("MESSAGE_HELP")).setParseMode("HTML");
        sendToChat(message);
    }

    private void buttonDeleteUsers(TelegramUser telegramUser) {
        telegramUser.setAction(TelegramUserAction.DELETE);
        telegramUserDao.update(telegramUser);
        sendToChat(telegramUser.getId(), Config.getString("MESSAGE_PROVIDE_USERNAMES"));
    }

    private void deleteTwitterUsers(TelegramUser telegramUser, String text) {
        if (telegramUser.getTargetedTwitterUsers() == null || telegramUser.getTargetedTwitterUsers().isEmpty()) {
            sendToChat(telegramUser.getId(), Config.getString("MESSAGE_DELETE_ERROR"));
            return;
        }
        Set<String> removedSuccessfully = new HashSet<>();
        Set<String> failedToRemove = new HashSet<>();
        List<String> usernamesToBeDeleted = Arrays.asList(text.split("\\s*,\\s*"));
        sendToChat(telegramUser.getId(), Config.getString("MESSAGE_RETURN_USERNAMES") + text);

        Map<String, Long> idsByUsername = twitterUtils.getIdByUsernameMap(usernamesToBeDeleted, telegramUser);
        for (HashMap.Entry<String, Long> entry : idsByUsername.entrySet()) {
            TelegramTwitterRelation relationDeleted = new TelegramTwitterRelation(telegramUser.getId(), entry.getValue(), TelegramTwitterRelationType.MANUAL_DELETED);
            TelegramTwitterRelation relationAll = new TelegramTwitterRelation(telegramUser.getId(), entry.getValue(), TelegramTwitterRelationType.MANUAL_ALL);

            if (telegramUser.getTargetedTwitterUsers().contains(relationDeleted)) {
                removedSuccessfully.add(entry.getKey());
                telegramUser.getTargetedTwitterUsers().remove(relationDeleted);
            } else {
                failedToRemove.add(entry.getKey());
            }
            if (telegramUser.getTargetedTwitterUsers().contains(relationAll)) {
                removedSuccessfully.add(entry.getKey());
                telegramUser.getTargetedTwitterUsers().remove(relationAll);
            } else if (!removedSuccessfully.contains(entry.getKey())) {
                failedToRemove.add(entry.getKey());
            }
        }

        telegramUser.setAction(null);
        telegramUserDao.update(telegramUser);
        twitterStreamHelper.queueRestartStream();

        String responseText = "";
        if (!removedSuccessfully.isEmpty()) {
            responseText = responseText + Config.getString("MESSAGE_DELETE_SUCCESS")
                    + String.join(", ", removedSuccessfully);
        }
        if (!failedToRemove.isEmpty()) {
            if (removedSuccessfully.isEmpty()) {
                responseText = responseText + Config.getString("MESSAGE_CANNOT_DELETE")
                        + String.join(", ", failedToRemove);
            } else {
                responseText = responseText + "\n" + Config.getString("MESSAGE_CANNOT_DELETE")
                        + String.join(", ", failedToRemove);
            }
        }
        if (responseText.isEmpty()) {
            responseText = Config.getString("MESSAGE_DELETE_USERS_ERROR");
        }
        sendToChat(telegramUser.getId(), responseText);
    }

    private void buttonTrackAll(TelegramUser telegramUser) {
        telegramUser.setAction(TelegramUserAction.TRACK_ALL);
        telegramUserDao.update(telegramUser);
        sendToChat(telegramUser.getId(), Config.getString("MESSAGE_PROVIDE_USERNAMES"));
    }

    private void buttonTrackDeleted(TelegramUser telegramUser) {
        telegramUser.setAction(TelegramUserAction.TRACK_DELETED);
        telegramUserDao.update(telegramUser);
        sendToChat(telegramUser.getId(), Config.getString("MESSAGE_PROVIDE_USERNAMES"));
    }

    private void trackAllOrDeletedTweets(TelegramUser telegramUser, String text) {
        Set<String> addedSuccessfully = new HashSet<>();
        Set<String> failedToAdd = new HashSet<>();
        List<String> usernamesToBeTracked = Arrays.asList(text.split("\\s*,\\s*"));
        sendToChat(telegramUser.getId(), Config.getString("MESSAGE_RETURN_USERNAMES") + text);

        Map<String, Long> idsByUsername = twitterUtils.getIdByUsernameMap(usernamesToBeTracked, telegramUser);
        for (HashMap.Entry<String, Long> entry : idsByUsername.entrySet()) {
            if (entry.getValue() != null) {
                TelegramTwitterRelation telegramTwitterRelation = new TelegramTwitterRelation(telegramUser, entry.getValue(), TelegramTwitterRelationType.MANUAL_DELETED);
                addedSuccessfully.add(entry.getKey());
                telegramUser.getTargetedTwitterUsers().add(telegramTwitterRelation);
                if (TelegramUserAction.TRACK_ALL.equals(telegramUser.getAction())) {
                    TelegramTwitterRelation relationAll = new TelegramTwitterRelation(telegramUser, entry.getValue(), TelegramTwitterRelationType.MANUAL_ALL);
                    telegramUser.getTargetedTwitterUsers().add(relationAll);
                }
            } else {
                failedToAdd.add(entry.getKey());
            }
        }

        telegramUser.setAction(null);
        telegramUserDao.update(telegramUser);
        twitterStreamHelper.queueRestartStream();

        String responseText = "";
        if (!addedSuccessfully.isEmpty()) {
            responseText = responseText + Config.getString("MESSAGE_ADD_SUCCESS")
                    + String.join(", ", addedSuccessfully);
        }
        if (!failedToAdd.isEmpty()) {
            if (addedSuccessfully.isEmpty()) {
                responseText = responseText + Config.getString("MESSAGE_CANNOT_ADD")
                        + String.join(", ", failedToAdd);
            } else {
                responseText = responseText + "\n" + Config.getString("MESSAGE_CANNOT_ADD")
                        + String.join(", ", failedToAdd);
            }
        }
        sendToChat(telegramUser.getId(), responseText);
    }

    private void actionAuth(TelegramUser telegramUser, String text) {
        if (!isNumeric(text)) {
            sendToChat(telegramUser.getId(), Config.getString("MESSAGE_WRONG_PIN_ERROR"));
        } else {
            saveAccessToken(telegramUser, text);
        }
    }

    private void saveAccessToken(TelegramUser telegramUser, String text) {
        AccessToken accessToken = twitterUtils.getAccessToken(text, telegramUser.getId());
        if (accessToken == null) {
            sendToChat(telegramUser.getId(), Config.getString("MESSAGE_SAVE_TOKEN_ERROR"));
            sendToChat(telegramUser.getId(), twitterUtils.getAuthorizationURL(telegramUser.getId()));
        } else {
            telegramUser.setAccessKey(accessToken.getToken());
            telegramUser.setAccessSecret(accessToken.getTokenSecret());
            Integer twitterFollowingAmount = getTwitterFollowingList(telegramUser);
            sendToChat(telegramUser.getId(), Config.getString("MESSAGE_SAVE_TOKEN"));
            if (twitterFollowingAmount == null) {
                sendToChat(telegramUser.getId(), Config.getString("MESSAGE_API_LIMIT"));
            } else {
                sendToChat(telegramUser.getId(), String.format(Config.getString("MESSAGE_REFRESH_USERS"), twitterFollowingAmount));
            }
            telegramUserDao.update(telegramUser);
            twitterStreamHelper.queueRestartStream();
            sendToChat(createSignedInMenuMarkup(telegramUser.getId()));
        }
    }

    private Integer getTwitterFollowingList(TelegramUser telegramUser) {
        telegramUser.getTargetedTwitterUsers().removeIf(telegramTwitterRelation -> TelegramTwitterRelationType.AUTO_DELETED.equals(telegramTwitterRelation.getType()));
        List<Long> twitterUsersTrackDeleted = twitterUtils.getFollowingList(telegramUser.getAccessKey(), telegramUser.getAccessSecret());
        //twitterUsersTrackDeleted == null if user have reached rate limit (15 calls per 15 minutes)
        if (twitterUsersTrackDeleted == null) {
            return null;
        }
        for (Long id : twitterUsersTrackDeleted) {
            telegramUser.getTargetedTwitterUsers().add(new TelegramTwitterRelation(telegramUser, id, TelegramTwitterRelationType.AUTO_DELETED));
        }
        return twitterUsersTrackDeleted.size();
    }

    private void sendToChat(Long id, String text) {
        SendMessage message = new SendMessage()
                .setChatId(id)
                .setText(text);
        sendToChat(message);
    }

    private void sendToChat(SendMessage msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error(e);
        }
    }

    private boolean isNumeric(String text) {
        if (text == null || text.length() == 0) {
            return false;
        }
        for (char c : text.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getBotUsername() {
        return Config.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return Config.BOT_TOKEN;
    }
}