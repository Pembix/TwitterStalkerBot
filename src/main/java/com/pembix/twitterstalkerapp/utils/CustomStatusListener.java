package com.pembix.twitterstalkerapp.utils;

import com.pembix.twitterstalkerapp.dao.TweetDao;
import com.pembix.twitterstalkerapp.model.Tweet;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.pembix.twitterstalkerapp.utils.ListsProvider.stalkingRelationsTrackAll;
import static com.pembix.twitterstalkerapp.utils.ListsProvider.stalkingRelationsTrackDeleted;

public class CustomStatusListener implements StatusListener {
    private final static Logger logger = Logger.getLogger(CustomStatusListener.class);

    private final static String removedSign = "\u274C";
    private Chat chat;
    private TweetDao tweetDao = TweetDao.implementation();

    public CustomStatusListener() {
    }

    public CustomStatusListener(Chat chat) {
        this.chat = chat;
    }

    void setTweetDao(TweetDao tweetDao) {
        this.tweetDao = tweetDao;
    }

    void setChat(Chat chat) {
        this.chat = chat;
    }

    //on tweet received
    public void onStatus(Status status) {
        Set<Long> telegramIdsTrackDeleted = stalkingRelationsTrackDeleted.get(status.getUser().getId());
        Set<Long> telegramIdsTrackAll = stalkingRelationsTrackAll.get(status.getUser().getId());

        if (telegramIdsTrackDeleted != null) {
            Tweet tweet = new Tweet(status.getId(), status.getUser().getId(), status.getUser().getName(),
                    status.getUser().getScreenName(), status.getText(), status.getCreatedAt().getTime());
            tweetDao.save(tweet);
        }
        if (telegramIdsTrackAll != null) {
            String textToSend = String.format("%s (#%s) - %s", status.getUser().getName(), status.getUser().getScreenName(), status.getText());
            List<SendMessage> messages = new ArrayList<>();
            for (Long id : telegramIdsTrackAll) {
                messages.add(new SendMessage().setChatId(id).setText(textToSend));
            }
            sendToChat(messages);
        }
    }

    //on a tweet was deleted
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        Set<Long> telegramIdsTrackDeleted = stalkingRelationsTrackDeleted.get(statusDeletionNotice.getUserId());
        if (telegramIdsTrackDeleted != null) {
            Tweet tweet = tweetDao.get(statusDeletionNotice.getStatusId());
            if (tweet != null) {
                String textToSend = String.format("%s %s (#%s) - %s", removedSign, tweet.getUsername(), tweet.getScreenname(), tweet.getText());
                List<SendMessage> messages = new ArrayList<>();
                for (Long id : telegramIdsTrackDeleted) {
                    messages.add(new SendMessage().setChatId(id).setText(textToSend));
                }
                sendToChat(messages);
            }
        }
    }

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
    }

    public void onScrubGeo(long userId, long upToStatusId) {
    }

    public void onStallWarning(StallWarning warning) {
    }

    public void onException(Exception ex) {
        logger.error(ex);
    }

    @SuppressWarnings("unchecked")
    private void sendToChat(List<SendMessage> messages) {
        try {
            for (SendMessage message : messages) {
                chat.send(message);
            }
        } catch (TelegramApiException e) {
            logger.error(e);
        }
    }
}

