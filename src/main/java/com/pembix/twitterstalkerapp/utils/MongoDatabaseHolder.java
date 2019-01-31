package com.pembix.twitterstalkerapp.utils;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class MongoDatabaseHolder {
    private static MongoDatabase instance = null;

    private MongoDatabaseHolder() {
    }

    public static MongoDatabase getInstance() {
        if (instance == null) {
            MongoClient mongoClient = new MongoClient(new MongoClientURI(Config.MONGO_DB_URL));
            String dbName = Config.MONGO_DB_URL.substring(Config.MONGO_DB_URL.lastIndexOf("/") + 1);
            instance = mongoClient.getDatabase(dbName);
        }
        return instance;
    }
}
