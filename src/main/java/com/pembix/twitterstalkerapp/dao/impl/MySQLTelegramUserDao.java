package com.pembix.twitterstalkerapp.dao.impl;

import com.pembix.twitterstalkerapp.dao.TelegramUserDao;
import com.pembix.twitterstalkerapp.model.TelegramTwitterRelation;
import com.pembix.twitterstalkerapp.model.TelegramUser;
import com.pembix.twitterstalkerapp.utils.Config;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

public class MySQLTelegramUserDao implements TelegramUserDao {

    private final static Logger logger = Logger.getLogger(MySQLTelegramUserDao.class);
    private EntityManager entityManager;

    public MySQLTelegramUserDao() {
        EntityManagerFactory factory = Persistence
                .createEntityManagerFactory(Config.PERSISTENCE_UNIT);
        entityManager = factory.createEntityManager();
    }

    @Override
    public void save(TelegramUser telegramUser) {
        entityManager.getTransaction().begin();
        entityManager.persist(telegramUser);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }

    @Override
    public TelegramUser get(long telegramUserId) {
        entityManager.getTransaction().begin();
        TelegramUser telegramUser = entityManager.find(TelegramUser.class, telegramUserId);
        entityManager.getTransaction().commit();
        return telegramUser;
    }

    @Override
    public List<TelegramTwitterRelation> getAllRelations() {
        entityManager.getTransaction().begin();
        /*String sqlRequest = "select TelegramTwitterRelation from TelegramTwitterRelation as rel " +
                "inner join TelegramUser as user on user.id = rel.relationPk.telegramUserId  where user.status = 'active'";
        List<TelegramTwitterRelation> relations = entityManager.createQuery(sqlRequest, TelegramTwitterRelation.class).getResultList();*/
        @SuppressWarnings("unchecked")
        List<TelegramTwitterRelation> relations = entityManager.createNativeQuery("SELECT a.telegram_user_id, a.twitter_user_id, a.type FROM telegram_twitter_relation AS a INNER JOIN telegram_user AS b ON b.id = a.telegram_user_id WHERE b.status = 'ACTIVE'", TelegramTwitterRelation.class).getResultList();
        logger.info("I got " + relations.size() + "relations");
        entityManager.getTransaction().commit();
        return relations;
    }

    @Override
    public void update(TelegramUser telegramUser) {
        entityManager.getTransaction().begin();
        entityManager.merge(telegramUser);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }

    @Override
    public void delete(long telegramUserId) {
        entityManager.getTransaction().begin();
        TelegramUser telegramUser = entityManager.find(TelegramUser.class, telegramUserId);
        entityManager.remove(telegramUser);
        entityManager.getTransaction().commit();
    }
}
