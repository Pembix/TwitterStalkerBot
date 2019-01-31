package com.pembix.twitterstalkerapp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "tweet")
public class Tweet {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "twitter_user_id")
    private long twitterUserId;

    @Column(name = "username")
    private String username;

    @Column(name = "screenname")
    private String screenname;

    @Column(name = "text")
    private String text;

    @Column(name = "created")
    private long created;

    public Tweet() {
    }

    public Tweet(long id) {
        this.id = id;
    }

    public Tweet(long id, long twitterUserId, String text, long created) {
        this.id = id;
        this.twitterUserId = twitterUserId;
        this.text = text;
        this.created = created;
    }

    public Tweet(long id, long twitterUserId, String username, String screenname, String text, long created) {
        this.id = id;
        this.twitterUserId = twitterUserId;
        this.username = username;
        this.screenname = screenname;
        this.text = text;
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTwitterUserId() {
        return twitterUserId;
    }

    public void setTwitterUserId(long twitterUserId) {
        this.twitterUserId = twitterUserId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getScreenname() {
        return screenname;
    }

    public void setScreenname(String screenname) {
        this.screenname = screenname;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "id=" + id +
                ", twitterUserId=" + twitterUserId +
                ", username='" + username + '\'' +
                ", screenname='" + screenname + '\'' +
                ", text='" + text + '\'' +
                ", created=" + created +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tweet tweet = (Tweet) o;
        return id == tweet.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
