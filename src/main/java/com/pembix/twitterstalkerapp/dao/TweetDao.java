package com.pembix.twitterstalkerapp.dao;

import com.pembix.twitterstalkerapp.dao.impl.MongoDBTweetDao;
import com.pembix.twitterstalkerapp.dao.impl.MySQLTweetDao;
import com.pembix.twitterstalkerapp.model.Tweet;
import com.pembix.twitterstalkerapp.utils.Config;

public interface TweetDao {

    /**
     * Get TweetDao implementation based on Config property.
     *
     * @return MongoDBTweetDao or MySQLTweetDao
     */
    static TweetDao implementation() {
        return Config.USE_MONGO_DB ? new MongoDBTweetDao() : new MySQLTweetDao();
    }

    /**
     * Save tweet
     *
     * @param tweet
     */
    void save(Tweet tweet);

    /**
     * Get tweet by id
     *
     * @param tweetId id
     * @return tweet
     */
    Tweet get(long tweetId);
}
