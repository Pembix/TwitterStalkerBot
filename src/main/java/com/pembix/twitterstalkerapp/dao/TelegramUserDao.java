package com.pembix.twitterstalkerapp.dao;

import com.pembix.twitterstalkerapp.dao.impl.MongoDBTelegramUserDao;
import com.pembix.twitterstalkerapp.dao.impl.MySQLTelegramUserDao;
import com.pembix.twitterstalkerapp.model.TelegramTwitterRelation;
import com.pembix.twitterstalkerapp.model.TelegramUser;
import com.pembix.twitterstalkerapp.utils.Config;

import java.util.List;

public interface TelegramUserDao {

    /**
     * Get TelegramUserDao implementation based on Config property.
     *
     * @return MongoDBTelegramUserDao or MySQLTelegramUserDao
     */
    static TelegramUserDao implementation() {
        return Config.USE_MONGO_DB ? new MongoDBTelegramUserDao() : new MySQLTelegramUserDao();
    }

    /**
     * Save telegram user
     *
     * @param telegramUser
     */
    void save(TelegramUser telegramUser);

    /**
     * Get telegram user
     *
     * @param telegramUserId user id
     * @return telegram user
     */
    TelegramUser get(long telegramUserId);

    /**
     * Get all relations of all telegram users
     *
     * @return list of all relations
     */
    List<TelegramTwitterRelation> getAllRelations();

    /**
     * Update telegram user
     *
     * @param telegramUser user
     */
    void update(TelegramUser telegramUser);

    /**
     * Delete telegram user
     *
     * @param telegramUserId user id
     */
    void delete(long telegramUserId);
}
