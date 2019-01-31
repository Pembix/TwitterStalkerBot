package com.pembix.twitterstalkerapp.utils;

import com.pembix.twitterstalkerapp.model.TelegramUser;
import org.junit.Test;
import org.mockito.Mockito;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.api.UsersResources;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class TwitterUtilsTest {

    private static TwitterUtils twitterUtils = Mockito.spy(TwitterUtils.class);
    private static TelegramUser user = new TelegramUser(42L);

    @Test
    public void getIdByUsername() throws TwitterException {
        Twitter twitter = Mockito.mock(Twitter.class);
        List<String> usernames = new ArrayList<>();
        usernames.add("oneuser");
        usernames.add("twouser");
        usernames.add("threeuser");
        User twitterUser1 = Mockito.mock(User.class);
        User twitterUser2 = Mockito.mock(User.class);
        UsersResources usersResources = Mockito.mock(UsersResources.class);
        when(twitterUser1.getId()).thenReturn(34L);
        when(twitterUser2.getId()).thenReturn(35L);
        when(twitterUtils.getTwitterInstance()).thenReturn(twitter);
        when(twitter.users()).thenReturn(usersResources);
        when(usersResources.showUser("oneuser")).thenReturn(twitterUser1);
        when(usersResources.showUser("twouser")).thenReturn(twitterUser2);
        when(usersResources.showUser("threeuser")).thenThrow(new TwitterException("exception"));

        Map<String, Long> result = twitterUtils.getIdByUsernameMap(usernames, user);

        verify(usersResources, times(3)).showUser(any());
        assertEquals(3, result.size());
        assertEquals(twitterUser1.getId(), (long) result.get("oneuser"));
        assertEquals(twitterUser2.getId(), (long) result.get("twouser"));
        assertNull(result.get("threeuser"));
    }

    @Test
    public void getAuthorizationURLNotException() throws TwitterException {
        Twitter twitter = Mockito.mock(Twitter.class);
        Map<Long, RequestToken> requestTokenByUser = new HashMap<>();
        twitterUtils.setMap(requestTokenByUser);
        when(twitterUtils.getTwitterInstance()).thenReturn(twitter);
        when(twitter.getOAuthRequestToken()).thenReturn(new RequestToken("token", "tokenSecret"));

        String url = twitterUtils.getAuthorizationURL(user.getId());

        assertNotNull(requestTokenByUser.get(user.getId()));
        assertNotEquals(url, Config.getString("MESSAGE_ERROR"));
    }

    @Test
    public void getAuthorizationURLException() throws TwitterException {
        Twitter twitter = Mockito.mock(Twitter.class);
        Map<Long, RequestToken> requestTokenByUser = new HashMap<>();
        twitterUtils.setMap(requestTokenByUser);
        when(twitterUtils.getTwitterInstance()).thenReturn(twitter);
        when(twitter.getOAuthRequestToken()).thenThrow(new TwitterException("ex"));

        String url = twitterUtils.getAuthorizationURL(user.getId());

        assertEquals(url, Config.getString("MESSAGE_ERROR"));
        assertNull(requestTokenByUser.get(user.getId()));
    }

    @Test
    public void getAccessTokenNotNull() throws TwitterException {
        Map<Long, RequestToken> requestTokenByUser = new HashMap<>();
        RequestToken requestToken = new RequestToken("token", "tokenSecret");
        requestTokenByUser.put(user.getId(), requestToken);
        twitterUtils.setMap(requestTokenByUser);
        when(twitterUtils.getTwitterInstance().getOAuthAccessToken(requestToken, "1234567")).thenReturn(new AccessToken("token", "tokenSecret"));

        AccessToken accessToken = twitterUtils.getAccessToken("1234567", user.getId());

        assertNotNull(accessToken);
    }

    @Test
    public void getAccessTokenNull() throws TwitterException {
        Twitter twitter = Mockito.mock(Twitter.class);
        Map<Long, RequestToken> requestTokenByUser = new HashMap<>();
        RequestToken requestToken = new RequestToken("token", "tokenSecret");
        requestTokenByUser.put(user.getId(), requestToken);
        twitterUtils.setMap(requestTokenByUser);
        when(twitterUtils.getTwitterInstance()).thenReturn(twitter);
        when(twitter.getOAuthAccessToken(requestToken, "1234567")).thenReturn(null);

        AccessToken accessToken = twitterUtils.getAccessToken("1234567", user.getId());

        assertNull(accessToken);
    }
}
