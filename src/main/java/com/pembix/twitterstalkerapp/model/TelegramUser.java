package com.pembix.twitterstalkerapp.model;

import com.pembix.twitterstalkerapp.model.enums.TelegramUserAction;
import com.pembix.twitterstalkerapp.model.enums.TelegramUserStatus;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "telegram_user")
public class TelegramUser {

    @Id
    @Column(name = "id", updatable = false, nullable = false, insertable = false)
    private long id;

    @Column(name = "access_key")
    private String accessKey;

    @Column(name = "access_secret")
    private String accessSecret;

    @Column(name = "language")
    private String language;

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private TelegramUserAction action;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TelegramUserStatus status;

    @OneToMany(mappedBy = "telegramUser", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<TelegramTwitterRelation> targetedTwitterUsers = new HashSet<>();


    public TelegramUser() {
    }

    public TelegramUser(long id) {
        this.id = id;
        this.setStatus(TelegramUserStatus.ACTIVE);
    }

    public TelegramUser(long id, String accessKey, String accessSecret) {
        this.id = id;
        this.accessKey = accessKey;
        this.accessSecret = accessSecret;
    }

    public TelegramUser(long id, String accessKey, String accessSecret, String language) {
        this.id = id;
        this.accessKey = accessKey;
        this.accessSecret = accessSecret;
        this.language = language;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public TelegramUserAction getAction() {
        return action;
    }

    public void setAction(TelegramUserAction action) {
        this.action = action;
    }

    public TelegramUserStatus getStatus() {
        return status;
    }

    public void setStatus(TelegramUserStatus status) {
        this.status = status;
    }

    public Set<TelegramTwitterRelation> getTargetedTwitterUsers() {
        return targetedTwitterUsers;
    }

    public void setTargetedTwitterUsers(Set<TelegramTwitterRelation> targetedTwitterUsers) {
        this.targetedTwitterUsers = targetedTwitterUsers;
    }

    @Override
    public String toString() {
        return "TelegramUser{" +
                "id=" + id +
                ", accessKey='" + accessKey + '\'' +
                ", accessSecret='" + accessSecret + '\'' +
                ", language='" + language + '\'' +
                ", action='" + action + '\'' +
                ", status='" + status + '\'' +
                ", targetedTwitterUsers=" + targetedTwitterUsers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TelegramUser that = (TelegramUser) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isTwitterAuthorized() {
        return this.accessKey != null && this.accessSecret != null;
    }
}
