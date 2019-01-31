package com.pembix.twitterstalkerapp.dao.impl;

import com.pembix.twitterstalkerapp.dao.TweetDao;
import com.pembix.twitterstalkerapp.model.Tweet;
import com.pembix.twitterstalkerapp.utils.Config;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class MySQLTweetDao implements TweetDao {

    private EntityManager entityManager;

    public MySQLTweetDao() {
        EntityManagerFactory factory = Persistence
                .createEntityManagerFactory(Config.PERSISTENCE_UNIT);
        entityManager = factory.createEntityManager();
    }

    @Override
    public void save(Tweet tweet) {
        entityManager.getTransaction().begin();
        entityManager.persist(tweet);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }

    @Override
    public Tweet get(long tweetId) {
        entityManager.getTransaction().begin();
        Tweet tweet = entityManager.find(Tweet.class, tweetId);
        entityManager.getTransaction().commit();
        return tweet;
    }
}
