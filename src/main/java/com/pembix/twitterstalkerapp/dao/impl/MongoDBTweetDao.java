package com.pembix.twitterstalkerapp.dao.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.pembix.twitterstalkerapp.dao.TweetDao;
import com.pembix.twitterstalkerapp.model.Tweet;
import com.pembix.twitterstalkerapp.utils.MongoDatabaseHolder;
import org.bson.Document;

import java.util.Date;

public class MongoDBTweetDao implements TweetDao {

    private MongoCollection<Document> collection;

    public MongoDBTweetDao() {
        this.collection = MongoDatabaseHolder.getInstance().getCollection("Tweets");
    }

    MongoDBTweetDao(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    @Override
    public void save(Tweet tweet) {
        collection.insertOne(toMongoCreateObject(tweet));
    }

    @Override
    public Tweet get(long tweetId) {
        FindIterable<Document> resultSet = collection.find(new Document().append("_id", tweetId));
        Document tweet = resultSet.first();
        if (tweet != null) {
            return toTweet(tweet);
        }
        return null;
    }

    public Document toMongoCreateObject(Tweet tweet) {
        Document documentTweet = new Document("_id", tweet.getId())
                .append("twitterUserId", tweet.getTwitterUserId())
                .append("username", tweet.getUsername())
                .append("screenname", tweet.getScreenname())
                .append("text", tweet.getText())
                .append("created", new Date(tweet.getCreated()));
        return documentTweet;
    }

    public Tweet toTweet(Document document) {
        Tweet tweet = new Tweet();
        tweet.setId(document.getLong("_id"));
        tweet.setTwitterUserId(document.getLong("twitterUserId"));
        tweet.setUsername(document.getString("username"));
        tweet.setScreenname(document.getString("screenname"));
        tweet.setText(document.getString("text"));
        tweet.setCreated(document.getDate("created").getTime());
        return tweet;
    }
}
