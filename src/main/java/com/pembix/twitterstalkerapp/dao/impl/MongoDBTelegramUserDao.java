package com.pembix.twitterstalkerapp.dao.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.pembix.twitterstalkerapp.dao.TelegramUserDao;
import com.pembix.twitterstalkerapp.model.TelegramTwitterRelation;
import com.pembix.twitterstalkerapp.model.TelegramUser;
import com.pembix.twitterstalkerapp.model.enums.TelegramTwitterRelationType;
import com.pembix.twitterstalkerapp.model.enums.TelegramUserAction;
import com.pembix.twitterstalkerapp.model.enums.TelegramUserStatus;
import com.pembix.twitterstalkerapp.utils.MongoDatabaseHolder;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDBTelegramUserDao implements TelegramUserDao {

    private final static Logger logger = Logger.getLogger(MongoDBTelegramUserDao.class);
    private MongoCollection<Document> collection;

    public MongoDBTelegramUserDao() {
        this.collection = MongoDatabaseHolder.getInstance().getCollection("TelegramUser");
    }

    MongoDBTelegramUserDao(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    @Override
    public void save(TelegramUser telegramUser) {
        collection.insertOne(toMongoCreateObject(telegramUser));
    }

    @Override
    public TelegramUser get(long telegramUserId) {
        FindIterable<Document> resultSet = collection.find(new Document().append("_id", telegramUserId));
        Document user = resultSet.first();
        if (user != null) {
            return toTelegramUser(user);
        }
        return null;
    }

    @Override
    public List<TelegramTwitterRelation> getAllRelations() {
        List<TelegramTwitterRelation> relations = new ArrayList<>();
        FindIterable<Document> resultSet = collection.find(new Document().append("status", "ACTIVE"));

        for (Document document : resultSet) {
            @SuppressWarnings("unchecked")
            List<Document> documentRelations = document.get("targetedTwitterUsers", ArrayList.class);
            for (Document user : documentRelations) {
                relations.add(new TelegramTwitterRelation(toTelegramUser(document), user.getLong("twitterUserId"),
                        TelegramTwitterRelationType.from(user.getString("type"))));
            }
        }
        logger.info("I got " + relations.size() + " relations.");
        return relations;
    }


    @Override
    public void update(TelegramUser telegramUser) {
        collection.updateOne(new Document().append("_id", telegramUser.getId()), toMongoUpdateObject(telegramUser));
    }

    @Override
    public void delete(long telegramUserId) {
        collection.deleteOne(new Document().append("_id", telegramUserId));
    }

    public Document toMongoCreateObject(TelegramUser telegramUser) {
        List<Document> targetedTwitterUsers = new ArrayList<>();

        for (TelegramTwitterRelation relation : telegramUser.getTargetedTwitterUsers()) {
            targetedTwitterUsers.add(new Document("twitterUserId", relation.getTwitterUserId())
                    .append("type", relation.getType().name()));
        }

        Document documentUser = new Document("_id", telegramUser.getId())
                .append("accessKey", telegramUser.getAccessKey())
                .append("accessSecret", telegramUser.getAccessSecret())
                .append("action", telegramUser.getAction() == null ? null : telegramUser.getAction().name())
                .append("status", telegramUser.getStatus().name())
                .append("targetedTwitterUsers", targetedTwitterUsers);
        return documentUser;
    }

    public Document toMongoUpdateObject(TelegramUser telegramUser) {
        List<Document> targetedTwitterUsers = new ArrayList<>();

        for (TelegramTwitterRelation relation : telegramUser.getTargetedTwitterUsers()) {
            targetedTwitterUsers.add(new Document("twitterUserId", relation.getTwitterUserId())
                    .append("type", relation.getType().name()));
        }

        Document documentUser = new Document("accessKey", telegramUser.getAccessKey())
                .append("accessSecret", telegramUser.getAccessSecret())
                .append("action", telegramUser.getAction() == null ? null : telegramUser.getAction().name())
                .append("status", telegramUser.getStatus().name())
                .append("targetedTwitterUsers", targetedTwitterUsers);
        return new Document("$set", documentUser);
    }

    public TelegramUser toTelegramUser(Document document) {
        TelegramUser telegramUser = new TelegramUser();
        telegramUser.setId(document.getLong("_id"));
        telegramUser.setAccessKey(document.getString("accessKey"));
        telegramUser.setAccessSecret(document.getString("accessSecret"));
        telegramUser.setAction(TelegramUserAction.from(document.getString("action")));
        telegramUser.setStatus(TelegramUserStatus.from(document.getString("status")));
        @SuppressWarnings("unchecked")
        List<Document> users = document.get("targetedTwitterUsers", ArrayList.class);
        for (Document user : users) {
            telegramUser.getTargetedTwitterUsers().add(new TelegramTwitterRelation(telegramUser, user.getLong("twitterUserId"),
                    TelegramTwitterRelationType.from(user.getString("type"))));
        }
        return telegramUser;
    }
}
