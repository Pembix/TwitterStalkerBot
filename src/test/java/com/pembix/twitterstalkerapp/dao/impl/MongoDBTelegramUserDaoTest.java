package com.pembix.twitterstalkerapp.dao.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.pembix.twitterstalkerapp.model.TelegramTwitterRelation;
import com.pembix.twitterstalkerapp.model.TelegramUser;
import com.pembix.twitterstalkerapp.model.enums.TelegramTwitterRelationType;
import com.pembix.twitterstalkerapp.model.enums.TelegramUserAction;
import com.pembix.twitterstalkerapp.model.enums.TelegramUserStatus;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MongoDBTelegramUserDaoTest {
    private static final MongodStarter starter = MongodStarter.getDefaultInstance();

    private static MongodExecutable mongodExe;
    private static MongodProcess mongod;

    private static MongoClient mongo;
    private static MongoCollection<Document> collection;
    private static MongoDBTelegramUserDao telegramUserDao;

    @BeforeClass
    public static void setUp() throws Exception {
        int mongoPort = Network.getFreeServerPort();
        mongodExe = starter.prepare(new MongodConfigBuilder()
                .version(Version.Main.V3_5)
                .net(new Net("localhost", mongoPort, Network.localhostIsIPv6()))
                .build());
        mongod = mongodExe.start();

        mongo = new MongoClient("localhost", mongoPort);
        collection = mongo.getDatabase("StalkerApp").getCollection("TelegramUser");
        telegramUserDao = new MongoDBTelegramUserDao(collection);
    }

    @AfterClass
    public static void tearDown() {
        mongod.stop();
        mongodExe.stop();
    }

    @Test
    public void get() {
        //GIVEN
        TelegramUser telegramUser = new TelegramUser(49L);
        collection.insertOne(telegramUserDao.toMongoCreateObject(telegramUser));

        //WHEN
        TelegramUser actualUser = telegramUserDao.get(49L);

        //THEN
        assertNotNull(actualUser);
        assertEquals(49L, actualUser.getId());
    }

    @Test
    public void get2() {
        //GIVEN
        TelegramUser telegramUser = new TelegramUser(50L);
        collection.insertOne(telegramUserDao.toMongoCreateObject(telegramUser));

        //WHEN
        TelegramUser actualUser = telegramUserDao.get(48L);

        //THEN
        assertNull(actualUser);
    }

    @Test
    public void create() {
        //GIVEN
        TelegramUser telegramUser = new TelegramUser(63L);

        //WHEN
        telegramUserDao.save(telegramUser);

        //THEN
        TelegramUser actualUser = telegramUserDao.toTelegramUser(collection.find(new Document().append("_id", 63L)).first());
        assertNotNull(actualUser);
        assertEquals(63L, actualUser.getId());
    }

    @Test
    public void update() {
        //GIVEN
        TelegramUser telegramUser = new TelegramUser(64L);
        collection.insertOne(telegramUserDao.toMongoCreateObject(telegramUser));
        telegramUser.setAction(TelegramUserAction.DELETE);
        telegramUser.setAccessSecret("accessSecret");
        telegramUser.setAccessKey("accessKey");
        telegramUser.getTargetedTwitterUsers().add(new TelegramTwitterRelation(telegramUser, 345L,
                TelegramTwitterRelationType.MANUAL_DELETED));

        //WHEN
        telegramUserDao.update(telegramUser);

        //THEN
        TelegramUser actualUser = telegramUserDao.toTelegramUser(collection.find(new Document().append("_id", 64L)).first());
        assertNotNull(actualUser);
        Assert.assertEquals(TelegramUserAction.DELETE, actualUser.getAction());
        assertEquals("accessSecret", actualUser.getAccessSecret());
        assertEquals("accessKey", actualUser.getAccessKey());
        assertEquals(1, actualUser.getTargetedTwitterUsers().size());
    }

    @Test
    public void getAll() {
        //GIVEN
        collection.deleteMany(new Document());

        TelegramUser telegramUser = new TelegramUser(65L);
        telegramUser.getTargetedTwitterUsers().add(new TelegramTwitterRelation(telegramUser, 345L,
                TelegramTwitterRelationType.MANUAL_DELETED));
        telegramUser.getTargetedTwitterUsers().add(new TelegramTwitterRelation(telegramUser, 347L,
                TelegramTwitterRelationType.MANUAL_ALL));

        TelegramUser telegramUser2 = new TelegramUser(66L);
        telegramUser2.getTargetedTwitterUsers().add(new TelegramTwitterRelation(telegramUser2, 351L,
                TelegramTwitterRelationType.MANUAL_ALL));
        telegramUser2.getTargetedTwitterUsers().add(new TelegramTwitterRelation(telegramUser2, 352L,
                TelegramTwitterRelationType.MANUAL_DELETED));

        TelegramUser telegramUser3 = new TelegramUser(78L);
        telegramUser3.getTargetedTwitterUsers().add(new TelegramTwitterRelation(telegramUser3, 351L,
                TelegramTwitterRelationType.MANUAL_ALL));
        telegramUser3.setStatus(TelegramUserStatus.INACTIVE);

        collection.insertOne(telegramUserDao.toMongoCreateObject(telegramUser));
        collection.insertOne(telegramUserDao.toMongoCreateObject(telegramUser2));
        collection.insertOne(telegramUserDao.toMongoCreateObject(telegramUser3));

        //WHEN
        List<TelegramTwitterRelation> relations = telegramUserDao.getAllRelations();

        //THEN
        assertEquals(4, relations.size());
    }
}
