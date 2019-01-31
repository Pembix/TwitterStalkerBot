package com.pembix.twitterstalkerapp.dao.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.pembix.twitterstalkerapp.dao.impl.MongoDBTweetDao;
import com.pembix.twitterstalkerapp.model.Tweet;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class MongoDBTweetDaoTest {
    private static final MongodStarter starter = MongodStarter.getDefaultInstance();

    private static MongodExecutable mongodExe;
    private static MongodProcess mongod;

    private static MongoClient mongo;
    private static MongoCollection<Document> collection;
    private static MongoDBTweetDao tweetDao;

    @BeforeClass
    public static void setUp() throws Exception {
        int mongoPort = Network.getFreeServerPort();
        mongodExe = starter.prepare(new MongodConfigBuilder()
                .version(Version.Main.V3_5)
                .net(new Net("localhost", mongoPort, Network.localhostIsIPv6()))
                .build());
        mongod = mongodExe.start();

        mongo = new MongoClient("localhost", mongoPort);
        collection = mongo.getDatabase("StalkerApp").getCollection("Tweets");
        tweetDao = new MongoDBTweetDao(collection);
    }

    @AfterClass
    public static void tearDown() {
        mongod.stop();
        mongodExe.stop();
    }

    @Test
    public void get() {
        //GIVEN
        Tweet tweet = new Tweet(25L);
        collection.insertOne(tweetDao.toMongoCreateObject(tweet));

        //WHEN
        Tweet actualTweet = tweetDao.get(25L);

        //THEN
        assertNotNull(actualTweet);
        assertEquals(25L, actualTweet.getId());
    }

    @Test
    public void get2() {
        //GIVEN

        //WHEN
        Tweet actualTweet = tweetDao.get(26L);

        //THEN
        assertNull(actualTweet);
    }

    @Test
    public void create() {
        //GIVEN
        Tweet tweet = new Tweet(63L);

        //WHEN
        tweetDao.save(tweet);

        //THEN
        Tweet actualTweet = tweetDao.toTweet(collection.find(new Document().append("_id", 63L)).first());
        assertNotNull(actualTweet);
        assertEquals(63L, actualTweet.getId());
    }
}
