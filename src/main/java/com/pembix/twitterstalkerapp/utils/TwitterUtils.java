package com.pembix.twitterstalkerapp.utils;

import com.pembix.twitterstalkerapp.model.TelegramUser;
import org.apache.log4j.Logger;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class TwitterUtils {
    private final static Logger logger = Logger.getLogger(TwitterUtils.class);

    //this should be moved to db?
    private static Map<Long, RequestToken> requestTokenByUser = new HashMap<>();

    public TwitterUtils() {
    }

    void setMap(Map<Long, RequestToken> requestTokens) {
        requestTokenByUser = requestTokens;
    }

    public Map<String, Long> getIdByUsernameMap(List<String> usernames, TelegramUser telegramUser) {
        Map<String, Long> idByUsername = new HashMap<>();
        Twitter twitter = getTwitterInstance();
        AccessToken accessToken;
        if (!telegramUser.isTwitterAuthorized()) {
            accessToken = Config.ACCESS_TOKEN;
        } else {
            accessToken = new AccessToken(telegramUser.getAccessKey(), telegramUser.getAccessSecret());
        }
        twitter.setOAuthAccessToken(accessToken);
        for (String username : usernames) {
            try {
                User twitterUser = twitter.users().showUser(username);
                idByUsername.put(username, twitterUser.getId());
            } catch (TwitterException e) {
                idByUsername.put(username, null);
                logger.debug("Can't get this twitterUser: " + username);
                logger.error(e);
            }
        }
        return idByUsername;
    }

    public List<Long> getFollowingList(String accessKey, String accessSecret) {
        AccessToken accessToken = new AccessToken(accessKey, accessSecret);
        IDs ids;
        try {
            Twitter twitter = getTwitterInstance();
            twitter.setOAuthAccessToken(accessToken);
            long cursor = -1;
            do {
                ids = twitter.getFriendsIDs(cursor);
            } while ((cursor = ids.getNextCursor()) != 0);
        } catch (TwitterException e) {
            logger.debug("Failed to get friends' ids: " + e.getMessage());
            logger.error(e);
            return null;
        }
        logger.info("Following total count: " + ids.getIDs().length);
        //filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        return Arrays.stream(ids.getIDs()).boxed().collect(Collectors.toList());
    }

    public String getAuthorizationURL(long telegramUserId) {
        Twitter twitter = getTwitterInstance();
        RequestToken requestToken;
        try {
            requestToken = twitter.getOAuthRequestToken();
            requestTokenByUser.put(telegramUserId, requestToken);
        } catch (TwitterException e) {
            logger.error(e);
            return Config.getString("MESSAGE_ERROR");
        }
        return requestToken.getAuthorizationURL();
    }

    public AccessToken getAccessToken(String pin, long telegramUserId) {
        Twitter twitter = getTwitterInstance();
        RequestToken requestToken = requestTokenByUser.get(telegramUserId);
        AccessToken accessToken = null;
        try {
            accessToken = twitter.getOAuthAccessToken(requestToken, pin);
        } catch (TwitterException e) {
            if (401 == e.getStatusCode()) {
                logger.warn("Unable to get the access token.");
            } else {
                logger.error("error", e);
            }
        } catch (IllegalStateException ie) {
            // access token is already available, or consumer key/secret is not set.
            if (!twitter.getAuthorization().isEnabled()) {
                logger.debug("OAuth consumer key/secret is not set.");
            }
        }
        return accessToken;
    }

    Twitter getTwitterInstance() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(Config.OAUTH_CONSUMER_KEY)
                .setOAuthConsumerSecret(Config.OAUTH_CONSUMER_SECRET);
        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }
}