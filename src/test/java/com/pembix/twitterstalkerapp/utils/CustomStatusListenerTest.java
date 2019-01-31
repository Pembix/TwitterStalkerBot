package com.pembix.twitterstalkerapp.utils;

import com.pembix.twitterstalkerapp.dao.TweetDao;
import com.pembix.twitterstalkerapp.model.TelegramUser;
import com.pembix.twitterstalkerapp.model.Tweet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyZeroInteractions;


public class CustomStatusListenerTest {

    private CustomStatusListener listener = Mockito.spy(CustomStatusListener.class);
    private TweetDao dao = Mockito.mock(TweetDao.class);
    private Chat chat = Mockito.mock(Chat.class);
    private ListsProvider listsProvider = Mockito.spy(ListsProvider.class);
    private TelegramUser user = new TelegramUser(42L);

    @Before
    public void before() {
        listener.setTweetDao(dao);
        listener.setChat(chat);
    }

    private Status getStatus() {
        Status status = Mockito.mock(Status.class);
        User twitterUser = Mockito.mock(User.class);
        Date date = Mockito.mock(Date.class);
        when(status.getUser()).thenReturn(twitterUser);
        when(status.getId()).thenReturn(45L);
        when(twitterUser.getName()).thenReturn("name");
        when(twitterUser.getScreenName()).thenReturn("screenName");
        when(status.getText()).thenReturn("text");
        when(status.getCreatedAt()).thenReturn(date);
        when(date.getTime()).thenReturn(1548418187L);
        when(twitterUser.getId()).thenReturn(32L);
        return status;
    }

    private StatusDeletionNotice getStatusDeletionNotice() {
        Tweet tweet = Mockito.mock(Tweet.class);
        StatusDeletionNotice notice = Mockito.mock(StatusDeletionNotice.class);
        when(notice.getUserId()).thenReturn(32L);
        when(notice.getStatusId()).thenReturn(123456L);
        when(dao.get(anyLong())).thenReturn(tweet);
        when(tweet.getScreenname()).thenReturn("screenName");
        when(tweet.getUsername()).thenReturn("userName");
        when(tweet.getText()).thenReturn("text");
        return notice;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onStatusHasAllAndDeleted() throws TelegramApiException {
        Set<Long> telegramIds = new HashSet<>();
        telegramIds.add(user.getId());
        Map<Long, Set<Long>> stalkingRelationsTrackDeleted = new HashMap<>();
        Map<Long, Set<Long>> stalkingRelationsTrackAll = new HashMap<>();
        Status status = getStatus();
        stalkingRelationsTrackDeleted.put(status.getUser().getId(), telegramIds);
        stalkingRelationsTrackAll.put(status.getUser().getId(), telegramIds);
        listsProvider.setMaps(stalkingRelationsTrackDeleted, stalkingRelationsTrackAll);

        listener.onStatus(status);

        verify(dao).save(any());
        verify(chat, times(1)).send(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onStatusNoTrackDeleted() throws TelegramApiException {
        Set<Long> telegramIds = new HashSet<>();
        telegramIds.add(user.getId());
        Map<Long, Set<Long>> stalkingRelationsTrackDeleted = new HashMap<>();
        Map<Long, Set<Long>> stalkingRelationsTrackAll = new HashMap<>();
        Status status = getStatus();
        //stalkingRelationsTrackDeleted.put(status.getUser().getId(), telegramIds);
        stalkingRelationsTrackAll.put(status.getUser().getId(), telegramIds);
        listsProvider.setMaps(stalkingRelationsTrackDeleted, stalkingRelationsTrackAll);

        listener.onStatus(status);

        verifyZeroInteractions(dao);
        verify(chat, times(1)).send(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onStatusNoTrackAll() {
        Set<Long> telegramIds = new HashSet<>();
        telegramIds.add(user.getId());
        Map<Long, Set<Long>> stalkingRelationsTrackDeleted = new HashMap<>();
        Map<Long, Set<Long>> stalkingRelationsTrackAll = new HashMap<>();
        Status status = getStatus();
        stalkingRelationsTrackDeleted.put(status.getUser().getId(), telegramIds);
        //stalkingRelationsTrackAll.put(status.getUser().getId(), telegramIds);
        listsProvider.setMaps(stalkingRelationsTrackDeleted, stalkingRelationsTrackAll);

        listener.onStatus(status);

        verify(dao).save(any());
        verifyZeroInteractions(chat);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onStatusNoTrackAllOrDeleted() {
        Set<Long> telegramIds = new HashSet<>();
        telegramIds.add(user.getId());
        Map<Long, Set<Long>> stalkingRelationsTrackDeleted = new HashMap<>();
        Map<Long, Set<Long>> stalkingRelationsTrackAll = new HashMap<>();
        Status status = getStatus();
        //stalkingRelationsTrackDeleted.put(status.getUser().getId(), telegramIds);
        //stalkingRelationsTrackAll.put(status.getUser().getId(), telegramIds);
        listsProvider.setMaps(stalkingRelationsTrackDeleted, stalkingRelationsTrackAll);

        listener.onStatus(status);

        verifyZeroInteractions(dao);
        verifyZeroInteractions(chat);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onDeletionNoticeHasDeleted() throws TelegramApiException {
        Set<Long> telegramIds = new HashSet<>();
        telegramIds.add(user.getId());
        Map<Long, Set<Long>> stalkingRelationsTrackDeleted = new HashMap<>();
        StatusDeletionNotice notice = getStatusDeletionNotice();
        stalkingRelationsTrackDeleted.put(notice.getUserId(), telegramIds);
        listsProvider.setMaps(stalkingRelationsTrackDeleted, null);

        listener.onDeletionNotice(notice);

        verify(dao, times(1)).get(anyLong());
        verify(chat, times(1)).send(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onDeletionNoticeNoTrackDeleted() {
        Set<Long> telegramIds = new HashSet<>();
        telegramIds.add(user.getId());
        Map<Long, Set<Long>> stalkingRelationsTrackDeleted = new HashMap<>();
        StatusDeletionNotice notice = getStatusDeletionNotice();
        //stalkingRelationsTrackDeleted.put(notice.getUserId(), telegramIds);
        listsProvider.setMaps(stalkingRelationsTrackDeleted, null);

        listener.onDeletionNotice(notice);

        verifyZeroInteractions(dao);
        verifyZeroInteractions(chat);
    }
}
