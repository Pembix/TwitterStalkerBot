package com.pembix.twitterstalkerapp.utils;

import com.pembix.twitterstalkerapp.dao.TelegramUserDao;
import com.pembix.twitterstalkerapp.model.TelegramTwitterRelation;
import com.pembix.twitterstalkerapp.model.enums.TelegramTwitterRelationType;

import java.util.*;

public class ListsProvider {

    private static TelegramUserDao telegramUserDao = TelegramUserDao.implementation();
    //contains a key == twitterUser id, value == set of telegram ids interested in activity of this twitterUsers
    public static Map<Long, Set<Long>> stalkingRelationsTrackDeleted = new HashMap<>();
    public static Map<Long, Set<Long>> stalkingRelationsTrackAll = new HashMap<>();
    //contains list of twitterUser ids
    public static Set<Long> twitterUsersIds = new HashSet<>();

    void setMaps(Map<Long, Set<Long>> stalkingRelationsTrackDeletedArg, Map<Long, Set<Long>> stalkingRelationsTrackAllArg) {
        stalkingRelationsTrackDeleted = stalkingRelationsTrackDeletedArg;
        stalkingRelationsTrackAll = stalkingRelationsTrackAllArg;
    }

    void setTelegramUserDao(TelegramUserDao telegramUserDao) {
        ListsProvider.telegramUserDao = telegramUserDao;
    }

    static {
        //dummy value
        twitterUsersIds.add(12L);
    }

    public static void update() {
        List<TelegramTwitterRelation> telegramTwitterRelationsList = telegramUserDao.getAllRelations();

        for (TelegramTwitterRelation relation : telegramTwitterRelationsList) {
            twitterUsersIds.add(relation.getTwitterUserId());

            if (TelegramTwitterRelationType.MANUAL_ALL.equals(relation.getType())) {
                Set<Long> telegramIds = stalkingRelationsTrackAll.getOrDefault(relation.getTwitterUserId(), new HashSet<>());
                telegramIds.add(relation.getTelegramUserId());
                stalkingRelationsTrackAll.put(relation.getTwitterUserId(), telegramIds);
            } else {
                Set<Long> telegramIds = stalkingRelationsTrackDeleted.getOrDefault(relation.getTwitterUserId(), new HashSet<>());
                telegramIds.add(relation.getTelegramUserId());
                stalkingRelationsTrackDeleted.put(relation.getTwitterUserId(), telegramIds);
            }
        }
    }
}
