package com.pembix.twitterstalkerapp.utils;

import org.apache.log4j.Logger;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TwitterStreamHelper is a util that helps to manage Twitter Stream.
 *
 * Current implementation of restartStream() performs shutdown() on stream
 * with following start of a new stream with new data. As simple as that.
 *
 * Previous version better managed restart of streams because in restartStream()
 * new stream was created with an empty listener, thus new stream was ready to listen
 * and after the old stream.shutdown() the new stream would obtain the right listener.
 * This version was removed due to rate limit from Twitter API as only one stream per ip can be opened.
 */
public class TwitterStreamHelper implements Runnable {
    private final static Logger logger = Logger.getLogger(TwitterStreamHelper.class);

    private static final AtomicBoolean run = new AtomicBoolean(false);
    private static CustomStatusListener listener;
    private static TwitterStream currentStream;

    public TwitterStreamHelper(Chat chat) {
        listener = new CustomStatusListener(chat);
    }

    public void startStream() {
        ListsProvider.update();
        currentStream = getTwitterStreamInstance();
        currentStream.addListener(listener);
        currentStream.filter(new FilterQuery(0, ListsProvider.twitterUsersIds.stream().mapToLong(l -> l).toArray()));
    }

    private void restartStream() {
        currentStream.shutdown();
        startStream();
    }

    public void queueRestartStream() {
        run.set(true);
    }

    private void check() {
        while (true) {
            try {
                if (run.get()) {
                    logger.debug("Restart of the stream was queued, now it's time to restart it.");
                    restartStream();
                    run.set(false);
                }
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }
    }

    @Override
    public void run() {
        check();
    }

    private TwitterStream getTwitterStreamInstance() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(Config.OAUTH_CONSUMER_KEY)
                .setOAuthConsumerSecret(Config.OAUTH_CONSUMER_SECRET);
        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
        TwitterStream st = tf.getInstance();
        st.setOAuthAccessToken(Config.ACCESS_TOKEN);
        return st;
    }
}

