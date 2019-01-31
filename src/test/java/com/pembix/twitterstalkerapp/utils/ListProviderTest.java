package com.pembix.twitterstalkerapp.utils;

import com.pembix.twitterstalkerapp.dao.TelegramUserDao;
import com.pembix.twitterstalkerapp.model.TelegramTwitterRelation;
import com.pembix.twitterstalkerapp.model.TelegramUser;
import com.pembix.twitterstalkerapp.model.enums.TelegramTwitterRelationType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class ListProviderTest {

    private ListsProvider listsProvider = Mockito.spy(ListsProvider.class);
    private TelegramUserDao dao = Mockito.mock(TelegramUserDao.class);

    @Before
    public void before() {
        listsProvider.setTelegramUserDao(dao);
    }

    @Test
    public void update() {
        List<TelegramTwitterRelation> testList = new ArrayList<>();
        testList.add(new TelegramTwitterRelation(new TelegramUser(47L), 345L, TelegramTwitterRelationType.MANUAL_DELETED));
        testList.add(new TelegramTwitterRelation(new TelegramUser(48L), 346L, TelegramTwitterRelationType.AUTO_DELETED));
        testList.add(new TelegramTwitterRelation(new TelegramUser(49L), 347L, TelegramTwitterRelationType.MANUAL_ALL));
        testList.add(new TelegramTwitterRelation(new TelegramUser(50L), 348L, TelegramTwitterRelationType.MANUAL_DELETED));
        when(dao.getAllRelations()).thenReturn(testList);
        Map<Long, Set<Long>> stalkingRelationsTrackDeleted = new HashMap<>();
        Map<Long, Set<Long>> stalkingRelationsTrackAll = new HashMap<>();
        listsProvider.setMaps(stalkingRelationsTrackDeleted, stalkingRelationsTrackAll);

        ListsProvider.update();

        //4+1 == because of static initialization in ListProvider class
        assertThat(ListsProvider.twitterUsersIds.size(), is(equalTo(4 + 1)));
        assertThat(ListsProvider.stalkingRelationsTrackDeleted.size(), is(equalTo(3)));
        assertThat(ListsProvider.stalkingRelationsTrackAll.size(), is(equalTo(1)));
    }
}
