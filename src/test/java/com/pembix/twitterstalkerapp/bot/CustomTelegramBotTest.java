package com.pembix.twitterstalkerapp.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pembix.twitterstalkerapp.dao.TelegramUserDao;
import com.pembix.twitterstalkerapp.model.TelegramTwitterRelation;
import com.pembix.twitterstalkerapp.model.TelegramUser;
import com.pembix.twitterstalkerapp.model.enums.TelegramTwitterRelationType;
import com.pembix.twitterstalkerapp.model.enums.TelegramUserAction;
import com.pembix.twitterstalkerapp.model.enums.TelegramUserStatus;
import com.pembix.twitterstalkerapp.utils.Config;
import com.pembix.twitterstalkerapp.utils.TwitterStreamHelper;
import com.pembix.twitterstalkerapp.utils.TwitterUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import twitter4j.auth.AccessToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class CustomTelegramBotTest {

    private CustomTelegramBot bot = Mockito.mock(CustomTelegramBot.class);
    private TelegramUserDao dao = Mockito.mock(TelegramUserDao.class);
    private TwitterStreamHelper twitterStreamHelper = Mockito.mock(TwitterStreamHelper.class);
    private TwitterUtils twitterUtils = Mockito.mock(TwitterUtils.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    private TelegramUser user = new TelegramUser(42L);

    @Before
    public void before() {
        doCallRealMethod().when(bot).setTelegramUserDao(dao);
        bot.setTelegramUserDao(dao);
        doCallRealMethod().when(bot).setTwitterStreamHelper(twitterStreamHelper);
        bot.setTwitterStreamHelper(twitterStreamHelper);
        doCallRealMethod().when(bot).setTwitterUtils(twitterUtils);
        bot.setTwitterUtils(twitterUtils);
        doCallRealMethod().when(bot).onUpdateReceived(any());

        user = new TelegramUser(42L);
        when(dao.get(user.getId())).thenReturn(user);
    }

    private Update getUpdate(String msg) {
        String json = String.format("{ \"message\": { \"text\": \"%s\", \"chat\": { \"id\": \"%s\" } } }", msg, user.getId());

        try {
            return objectMapper.readValue(json, Update.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new Update();
        }
    }

    private SendMessage getMessage(String text) {
        return new SendMessage()
                .setChatId(user.getId())
                .setText(text);
    }

    private void setAccessKeyAndSecret(TelegramUser telegramUser) {
        telegramUser.setAccessKey("qwerty");
        telegramUser.setAccessSecret("qwertyy");
    }

    @Test
    public void startNoUser() throws TelegramApiException {
        when(dao.get(user.getId())).thenReturn(null);
        Update update = getUpdate(Config.getString("COMMAND_START"));

        bot.onUpdateReceived(update);

        verify(dao).save(any());
        verify(bot).execute(getMessage(Config.getString("MESSAGE_START")));
        verify(bot).execute(TelegramMarkupCreator.createStartMenuMarkup(user.getId()));
    }

    @Test
    public void startUserActiveNoAccessKey() throws TelegramApiException {
        user.setStatus(TelegramUserStatus.ACTIVE);
        Update update = getUpdate(Config.getString("COMMAND_START"));

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_START")));
        verify(bot).execute(TelegramMarkupCreator.createStartMenuMarkup(user.getId()));
    }

    @Test
    public void startUserInactiveNoAccessKey() throws TelegramApiException {
        user.setStatus(TelegramUserStatus.INACTIVE);
        Update update = getUpdate(Config.getString("COMMAND_START"));

        bot.onUpdateReceived(update);

        assertEquals(TelegramUserStatus.ACTIVE, user.getStatus());
        verify(dao).update(user);
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(getMessage(Config.getString("MESSAGE_START")));
        verify(bot).execute(TelegramMarkupCreator.createStartMenuMarkup(user.getId()));
    }

    @Test
    public void startUserActiveHasAccessKey() throws TelegramApiException {
        user.setStatus(TelegramUserStatus.ACTIVE);
        setAccessKeyAndSecret(user);
        Update update = getUpdate(Config.getString("COMMAND_START"));

        bot.onUpdateReceived(update);

        verify(bot).execute(TelegramMarkupCreator.createSignedInMenuMarkup(user.getId()));
    }

    @Test
    public void startUserInactiveHasAccessKey() throws TelegramApiException {
        user.setStatus(TelegramUserStatus.INACTIVE);
        setAccessKeyAndSecret(user);
        Update update = getUpdate(Config.getString("COMMAND_START"));

        bot.onUpdateReceived(update);

        assertEquals(TelegramUserStatus.ACTIVE, user.getStatus());
        verify(dao).update(user);
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(TelegramMarkupCreator.createSignedInMenuMarkup(user.getId()));
    }

    @Test
    public void keyboardNoAccessKey() throws TelegramApiException {
        Update update = getUpdate(Config.getString("COMMAND_KEYBOARD"));

        bot.onUpdateReceived(update);

        verify(bot).execute(TelegramMarkupCreator.createStartMenuMarkup(user.getId()));
    }

    @Test
    public void keyboardUserHasAccessKey() throws TelegramApiException {
        Update update = getUpdate(Config.getString("COMMAND_KEYBOARD"));
        setAccessKeyAndSecret(user);

        bot.onUpdateReceived(update);

        verify(bot).execute(TelegramMarkupCreator.createSignedInMenuMarkup(user.getId()));
    }

    @Test
    public void stop() throws TelegramApiException {
        Update update = getUpdate(Config.getString("COMMAND_STOP"));

        bot.onUpdateReceived(update);

        assertEquals(TelegramUserStatus.INACTIVE, user.getStatus());
        verify(dao).update(user);
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(getMessage(Config.getString("MESSAGE_STOP")));
    }

    @Test
    public void delete() throws TelegramApiException {
        Update update = getUpdate(Config.getString("COMMAND_DELETE"));

        bot.onUpdateReceived(update);

        verify(dao).delete(user.getId());
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(getMessage(Config.getString("MESSAGE_DELETE")).setParseMode("HTML"));
    }

    @Test
    public void signInNoAccessKey() throws TelegramApiException {
        Update update = getUpdate(Config.getString("BUTTON_SIGN_IN"));

        bot.onUpdateReceived(update);

        assertEquals(TelegramUserAction.AUTH, user.getAction());
        verify(dao).update(user);
        verify(bot).execute(getMessage(Config.getString("MESSAGE_BEGIN_REG")));
        verify(twitterUtils).getAuthorizationURL(user.getId());
        verify(bot).execute(getMessage(twitterUtils.getAuthorizationURL(user.getId())));
    }

    @Test
    public void signInAlreadyRegistered() throws TelegramApiException {
        Update update = getUpdate(Config.getString("BUTTON_SIGN_IN"));
        setAccessKeyAndSecret(user);

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_BEGIN_REG_ERROR")));
    }

    @Test
    public void manageUsers() throws TelegramApiException {
        Update update = getUpdate(Config.getString("BUTTON_MANAGE_USERS"));
        user.setAction(TelegramUserAction.DELETE);

        bot.onUpdateReceived(update);

        assertNull(user.getAction());
        verify(bot).execute(TelegramMarkupCreator.createManageUsersMarkup(user.getId()));
    }

    @Test
    public void refresh() throws TelegramApiException {
        List<Long> twitterFollowingList = new ArrayList<>(5);
        Update update = getUpdate(Config.getString("BUTTON_REFRESH"));
        when(twitterUtils.getFollowingList(user.getAccessKey(), user.getAccessSecret())).thenReturn(twitterFollowingList);

        bot.onUpdateReceived(update);

        verify(twitterUtils).getFollowingList(user.getAccessKey(), user.getAccessSecret());
        verify(dao).update(user);
        verify(bot).execute(getMessage(String.format(Config.getString("MESSAGE_REFRESH_USERS"), twitterFollowingList.size())));
        verify(twitterStreamHelper).queueRestartStream();
    }

    @Test
    public void returnNoAccessKey() throws TelegramApiException {
        Update update = getUpdate(Config.getString("BUTTON_RETURN"));
        user.setAccessKey(null);
        user.setAccessSecret(null);

        bot.onUpdateReceived(update);

        verify(bot).execute(TelegramMarkupCreator.createStartMenuMarkup(user.getId()));
    }

    @Test
    public void returnHasAccessKey() throws TelegramApiException {
        Update update = getUpdate(Config.getString("BUTTON_RETURN"));
        setAccessKeyAndSecret(user);

        bot.onUpdateReceived(update);

        verify(bot).execute(TelegramMarkupCreator.createSignedInMenuMarkup(user.getId()));
    }

    @Test
    public void help() throws TelegramApiException {
        Update update = getUpdate(Config.getString("COMMAND_HELP"));

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_HELP")).setParseMode("HTML"));
    }

    @Test
    public void buttonDeleteUsers() throws TelegramApiException {
        Update update = getUpdate(Config.getString("BUTTON_DELETE_USERS"));

        bot.onUpdateReceived(update);

        assertEquals(TelegramUserAction.DELETE, user.getAction());
        verify(dao).update(user);
        verify(bot).execute(getMessage(Config.getString("MESSAGE_PROVIDE_USERNAMES")));
    }

    @Test
    public void deleteTwitterUsersNoTargetedUsers() throws TelegramApiException {
        user.setAction(TelegramUserAction.DELETE);
        Update update = getUpdate("oneuser, otheruser");

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_DELETE_ERROR")));
    }

    @Test
    public void deleteTwitterUsersHasTargetedUsersContainsDeletedAndAllSuccess() throws TelegramApiException {
        String text = "oneuser, otheruser";
        user.setAction(TelegramUserAction.DELETE);
        Update update = getUpdate(text);
        String[] usernamesArray = text.split("\\s*,\\s*");
        ArrayList<String> usernamesToBeDeleted = new ArrayList<>(Arrays.asList(usernamesArray));
        Long twitterUserId1 = 1234L;
        Long twitterUserId2 = 1235L;
        TelegramTwitterRelation relationDeleted1 = new TelegramTwitterRelation(user.getId(), twitterUserId1, TelegramTwitterRelationType.MANUAL_DELETED);
        TelegramTwitterRelation relationDeleted2 = new TelegramTwitterRelation(user.getId(), twitterUserId2, TelegramTwitterRelationType.MANUAL_DELETED);
        TelegramTwitterRelation relationAll2 = new TelegramTwitterRelation(user.getId(), twitterUserId2, TelegramTwitterRelationType.MANUAL_ALL);
        /*when we choose trackDeleted tweets of somebody, user.getTargetedTwitterUsers() should contain only
        MANUAL_DELETED TelegramTwitterRelation, but when we choose trackAll ---> user.getTargetedTwitterUsers()
        should contain both MANUAL_DELETED and MANUAL_ALL*/
        user.getTargetedTwitterUsers().add(relationDeleted1);
        user.getTargetedTwitterUsers().add(relationDeleted2);
        user.getTargetedTwitterUsers().add(relationAll2);
        HashMap<String, Long> idsByUsername = new HashMap<>();
        idsByUsername.put("oneuser", twitterUserId1);
        idsByUsername.put("otheruser", twitterUserId2);
        when(twitterUtils.getIdByUsernameMap(usernamesToBeDeleted, user)).thenReturn(idsByUsername);

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_RETURN_USERNAMES") + text));
        verify(twitterUtils).getIdByUsernameMap(usernamesToBeDeleted, user);
        assertEquals(user.getTargetedTwitterUsers().size(), 0);
        assertNull(user.getAction());
        verify(dao).update(user);
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(getMessage("These usernames have been deleted: otheruser, oneuser"));
    }

    @Test
    public void deleteTwitterUsersHasTargetedUsersContainsDeletedOnlySuccess() throws TelegramApiException {
        String text = "oneuser, otheruser";
        user.setAction(TelegramUserAction.DELETE);
        Update update = getUpdate(text);
        String[] usernamesArray = text.split("\\s*,\\s*");
        ArrayList<String> usernamesToBeDeleted = new ArrayList<>(Arrays.asList(usernamesArray));
        Long twitterUserId1 = 1234L;
        Long twitterUserId2 = 1235L;
        TelegramTwitterRelation relationDeleted1 = new TelegramTwitterRelation(user.getId(), twitterUserId1, TelegramTwitterRelationType.MANUAL_DELETED);
        TelegramTwitterRelation relationDeleted2 = new TelegramTwitterRelation(user.getId(), twitterUserId2, TelegramTwitterRelationType.MANUAL_DELETED);
        user.getTargetedTwitterUsers().add(relationDeleted1);
        user.getTargetedTwitterUsers().add(relationDeleted2);
        HashMap<String, Long> idsByUsername = new HashMap<>();
        idsByUsername.put("oneuser", twitterUserId1);
        idsByUsername.put("otheruser", twitterUserId2);
        when(twitterUtils.getIdByUsernameMap(usernamesToBeDeleted, user)).thenReturn(idsByUsername);

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_RETURN_USERNAMES") + text));
        verify(twitterUtils).getIdByUsernameMap(usernamesToBeDeleted, user);
        assertEquals(user.getTargetedTwitterUsers().size(), 0);
        assertNull(user.getAction());
        verify(dao).update(user);
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(getMessage("These usernames have been deleted: otheruser, oneuser"));
    }

    @Test
    public void deleteTwitterUsersHasTargetedUsersContainsDeletedAndAllOneFail() throws TelegramApiException {
        String text = "oneuser, otheruser";
        user.setAction(TelegramUserAction.DELETE);
        Update update = getUpdate(text);
        String[] usernamesArray = text.split("\\s*,\\s*");
        ArrayList<String> usernamesToBeDeleted = new ArrayList<>(Arrays.asList(usernamesArray));
        Long twitterUserId1 = 1234L;
        Long twitterUserId2 = 1235L;
        TelegramTwitterRelation relationDeleted1 = new TelegramTwitterRelation(user.getId(), twitterUserId1, TelegramTwitterRelationType.MANUAL_DELETED);
        TelegramTwitterRelation relationDeleted2 = new TelegramTwitterRelation(user.getId(), twitterUserId2, TelegramTwitterRelationType.MANUAL_DELETED);
        TelegramTwitterRelation relationAll2 = new TelegramTwitterRelation(user.getId(), twitterUserId2, TelegramTwitterRelationType.MANUAL_ALL);
        user.getTargetedTwitterUsers().add(relationDeleted1);
        user.getTargetedTwitterUsers().add(relationDeleted2);
        user.getTargetedTwitterUsers().add(relationAll2);
        HashMap<String, Long> idsByUsername = new HashMap<>();
        idsByUsername.put("oneuser", twitterUserId1);
        idsByUsername.put("otheruser", twitterUserId2);
        idsByUsername.put("fail", null);
        when(twitterUtils.getIdByUsernameMap(usernamesToBeDeleted, user)).thenReturn(idsByUsername);

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_RETURN_USERNAMES") + text));
        verify(twitterUtils).getIdByUsernameMap(usernamesToBeDeleted, user);
        assertEquals(user.getTargetedTwitterUsers().size(), 0);
        assertNull(user.getAction());
        verify(dao).update(user);
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(getMessage("These usernames have been deleted: otheruser, oneuser" + "\n" + "These usernames cannot be deleted: fail"));
    }

    @Test
    public void deleteTwitterUsersHasTargetedUsersNotContains() throws TelegramApiException {
        String text = "oneuser, otheruser";
        user.setAction(TelegramUserAction.DELETE);
        Update update = getUpdate(text);
        String[] usernamesArray = text.split("\\s*,\\s*");
        ArrayList<String> usernamesToBeDeleted = new ArrayList<>(Arrays.asList(usernamesArray));
        Long twitterUserId1 = 1234L;
        Long twitterUserId2 = 1235L;
        TelegramTwitterRelation relationDeleted1 = new TelegramTwitterRelation(user.getId(), twitterUserId1, TelegramTwitterRelationType.MANUAL_DELETED);
        TelegramTwitterRelation relationDeleted2 = new TelegramTwitterRelation(user.getId(), twitterUserId2, TelegramTwitterRelationType.MANUAL_DELETED);
        TelegramTwitterRelation relationAll2 = new TelegramTwitterRelation(user.getId(), twitterUserId2, TelegramTwitterRelationType.MANUAL_ALL);
        user.getTargetedTwitterUsers().add(relationDeleted1);
        user.getTargetedTwitterUsers().add(relationDeleted2);
        user.getTargetedTwitterUsers().add(relationAll2);
        HashMap<String, Long> idsByUsername = new HashMap<>();
        when(twitterUtils.getIdByUsernameMap(usernamesToBeDeleted, user)).thenReturn(idsByUsername);

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_RETURN_USERNAMES") + text));
        verify(twitterUtils).getIdByUsernameMap(usernamesToBeDeleted, user);
        assertEquals(user.getTargetedTwitterUsers().size(), 3);
        assertNull(user.getAction());
        verify(dao).update(user);
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(getMessage(Config.getString("MESSAGE_DELETE_USERS_ERROR")));
    }

    @Test
    public void buttonTrackAll() throws TelegramApiException {
        Update update = getUpdate(Config.getString("BUTTON_TRACK_ALL"));

        bot.onUpdateReceived(update);

        assertEquals(TelegramUserAction.TRACK_ALL, user.getAction());
        verify(dao).update(user);
        verify(bot).execute(getMessage(Config.getString("MESSAGE_PROVIDE_USERNAMES")));
    }

    @Test
    public void trackDeletedSuccess() throws TelegramApiException {
        String text = "oneuser, otheruser";
        user.setAction(TelegramUserAction.TRACK_DELETED);
        Update update = getUpdate(text);
        String[] usernamesArray = text.split("\\s*,\\s*");
        ArrayList<String> usernamesToBeDeleted = new ArrayList<>(Arrays.asList(usernamesArray));
        Long twitterUserId1 = 1234L;
        Long twitterUserId2 = 1235L;
        HashMap<String, Long> idsByUsername = new HashMap<>();
        idsByUsername.put("oneuser", twitterUserId1);
        idsByUsername.put("otheruser", twitterUserId2);
        when(twitterUtils.getIdByUsernameMap(usernamesToBeDeleted, user)).thenReturn(idsByUsername);

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_RETURN_USERNAMES") + text));
        verify(twitterUtils).getIdByUsernameMap(usernamesToBeDeleted, user);
        assertEquals(user.getTargetedTwitterUsers().size(), 2);
        assertNull(user.getAction());
        verify(dao).update(user);
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(getMessage("These usernames have been added: otheruser, oneuser"));
    }

    @Test
    public void trackDeletedFail() throws TelegramApiException {
        String text = "oneuser, otheruser";
        user.setAction(TelegramUserAction.TRACK_DELETED);
        Update update = getUpdate(text);
        String[] usernamesArray = text.split("\\s*,\\s*");
        ArrayList<String> usernamesToBeDeleted = new ArrayList<>(Arrays.asList(usernamesArray));
        Long twitterUserId1 = 1234L;
        Long twitterUserId2 = 1235L;
        HashMap<String, Long> idsByUsername = new HashMap<>();
        idsByUsername.put("oneuser", twitterUserId1);
        idsByUsername.put("otheruser", twitterUserId2);
        idsByUsername.put("fail", null);
        when(twitterUtils.getIdByUsernameMap(usernamesToBeDeleted, user)).thenReturn(idsByUsername);

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_RETURN_USERNAMES") + text));
        verify(twitterUtils).getIdByUsernameMap(usernamesToBeDeleted, user);
        assertEquals(user.getTargetedTwitterUsers().size(), 2);
        assertNull(user.getAction());
        verify(dao).update(user);
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(getMessage("These usernames have been added: otheruser, oneuser" + "\n" + "These usernames cannot be added: fail"));
    }

    @Test
    public void trackAllSuccess() throws TelegramApiException {
        String text = "oneuser, otheruser";
        user.setAction(TelegramUserAction.TRACK_ALL);
        Update update = getUpdate(text);
        String[] usernamesArray = text.split("\\s*,\\s*");
        ArrayList<String> usernamesToBeDeleted = new ArrayList<>(Arrays.asList(usernamesArray));
        Long twitterUserId1 = 1234L;
        Long twitterUserId2 = 1235L;
        HashMap<String, Long> idsByUsername = new HashMap<>();
        idsByUsername.put("oneuser", twitterUserId1);
        idsByUsername.put("otheruser", twitterUserId2);
        when(twitterUtils.getIdByUsernameMap(usernamesToBeDeleted, user)).thenReturn(idsByUsername);

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_RETURN_USERNAMES") + text));
        verify(twitterUtils).getIdByUsernameMap(usernamesToBeDeleted, user);
        assertEquals(user.getTargetedTwitterUsers().size(), 4);
        assertNull(user.getAction());
        verify(dao).update(user);
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(getMessage("These usernames have been added: otheruser, oneuser"));
    }

    @Test
    public void trackAllFail() throws TelegramApiException {
        String text = "oneuser, otheruser";
        user.setAction(TelegramUserAction.TRACK_ALL);
        Update update = getUpdate(text);
        String[] usernamesArray = text.split("\\s*,\\s*");
        ArrayList<String> usernamesToBeDeleted = new ArrayList<>(Arrays.asList(usernamesArray));
        Long twitterUserId1 = 1234L;
        Long twitterUserId2 = 1235L;
        HashMap<String, Long> idsByUsername = new HashMap<>();
        idsByUsername.put("oneuser", twitterUserId1);
        idsByUsername.put("otheruser", twitterUserId2);
        idsByUsername.put("fail", null);
        when(twitterUtils.getIdByUsernameMap(usernamesToBeDeleted, user)).thenReturn(idsByUsername);

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_RETURN_USERNAMES") + text));
        verify(twitterUtils).getIdByUsernameMap(usernamesToBeDeleted, user);
        assertEquals(user.getTargetedTwitterUsers().size(), 4);
        assertNull(user.getAction());
        verify(dao).update(user);
        verify(twitterStreamHelper).queueRestartStream();
        verify(bot).execute(getMessage("These usernames have been added: otheruser, oneuser" + "\n" + "These usernames cannot be added: fail"));
    }

    @Test
    public void buttonTrackDeleted() throws TelegramApiException {
        Update update = getUpdate(Config.getString("BUTTON_TRACK_DELETED"));

        bot.onUpdateReceived(update);

        assertEquals(TelegramUserAction.TRACK_DELETED, user.getAction());
        verify(dao).update(user);
        verify(bot).execute(getMessage(Config.getString("MESSAGE_PROVIDE_USERNAMES")));
    }

    @Test
    public void actionAuthIsNumericNoAccessToken() throws TelegramApiException {
        String text = "1234567";
        Update update = getUpdate(text);
        user.setAction(TelegramUserAction.AUTH);

        bot.onUpdateReceived(update);

        verify(twitterUtils).getAccessToken(text, user.getId());
        verify(bot).execute(getMessage(Config.getString("MESSAGE_SAVE_TOKEN_ERROR")));
        verify(bot).execute(getMessage(twitterUtils.getAuthorizationURL(user.getId())));

    }

    @Test
    public void actionAuthIsNumericHasAccessToken() throws TelegramApiException {
        String text = "1234567";
        List<Long> twitterFollowingList = new ArrayList<>(5);
        Update update = getUpdate(text);
        user.setAction(TelegramUserAction.AUTH);
        when(twitterUtils.getAccessToken(text, user.getId())).thenReturn(new AccessToken("newqwerty", "newqwertyy"));
        when(twitterUtils.getFollowingList(user.getAccessKey(), user.getAccessSecret())).thenReturn(twitterFollowingList);

        bot.onUpdateReceived(update);

        verify(twitterUtils).getAccessToken(text, user.getId());
        assertEquals("newqwerty", user.getAccessKey());
        assertEquals("newqwertyy", user.getAccessSecret());
        verify(twitterUtils).getFollowingList(user.getAccessKey(), user.getAccessSecret());
        verify(dao).update(user);
        verify(bot).execute(getMessage(Config.getString("MESSAGE_SAVE_TOKEN")));
        verify(bot).execute(TelegramMarkupCreator.createSignedInMenuMarkup(user.getId()));
        verify(bot).execute(getMessage(String.format(Config.getString("MESSAGE_REFRESH_USERS"), twitterFollowingList.size())));
        verify(twitterStreamHelper).queueRestartStream();
    }

    @Test
    public void actionAuthNotNumeric() throws TelegramApiException {
        Update update = getUpdate("notNumeric");
        user.setAction(TelegramUserAction.AUTH);

        bot.onUpdateReceived(update);

        verify(bot).execute(getMessage(Config.getString("MESSAGE_WRONG_PIN_ERROR")));
    }

    @Test
    public void invalidCommandInput() throws TelegramApiException {
        bot.onUpdateReceived(getUpdate("qwerty"));

        verify(bot).execute(getMessage(Config.getString("MESSAGE_UPDATE_ERROR")));
    }
}
