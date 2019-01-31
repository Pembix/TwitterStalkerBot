package com.pembix.twitterstalkerapp.model;

import com.pembix.twitterstalkerapp.model.enums.TelegramTwitterRelationType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "telegram_twitter_relation")
public class TelegramTwitterRelation {

    @Embeddable
    public static class RelationPk implements Serializable {

        @Column(name = "telegram_user_id", nullable = false, updatable = false)
        private Long telegramUserId;

        @Column(name = "twitter_user_id", nullable = false, updatable = false)
        private Long twitterUserId;

        @Column(name = "type", nullable = false, updatable = false)
        @Enumerated(EnumType.STRING)
        private TelegramTwitterRelationType type;

        public RelationPk() {
        }

        public RelationPk(Long telegramUserId, Long twitterUserId, TelegramTwitterRelationType type) {
            this.telegramUserId = telegramUserId;
            this.twitterUserId = twitterUserId;
            this.type = type;
        }

        public Long getTelegramUserId() {
            return telegramUserId;
        }

        public void setTelegramUserId(Long telegramUserId) {
            this.telegramUserId = telegramUserId;
        }

        public Long getTwitterUserId() {
            return twitterUserId;
        }

        public void setTwitterUserId(Long twitterUserId) {
            this.twitterUserId = twitterUserId;
        }

        public TelegramTwitterRelationType getType() {
            return type;
        }

        public void setType(TelegramTwitterRelationType type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RelationPk that = (RelationPk) o;
            return Objects.equals(telegramUserId, that.telegramUserId) &&
                    Objects.equals(twitterUserId, that.twitterUserId) &&
                    type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(telegramUserId, twitterUserId, type);
        }
    }

    @EmbeddedId
    private RelationPk relationPk;


    @ManyToOne
    @JoinColumn(name = "telegram_user_id", insertable = false, updatable = false)
    private TelegramUser telegramUser;


    public TelegramTwitterRelation() {
    }

    public TelegramTwitterRelation(Long id, Long twitterUserId, TelegramTwitterRelationType type) {
        RelationPk relationPk = new RelationPk(id, twitterUserId, type);
        this.setRelationPk(relationPk);
    }

    public TelegramTwitterRelation(TelegramUser telegramUser, Long twitterUserId, TelegramTwitterRelationType type) {
        RelationPk relationPk = new RelationPk(telegramUser.getId(), twitterUserId, type);
        this.setRelationPk(relationPk);
        this.setTelegramUser(telegramUser);
        telegramUser.getTargetedTwitterUsers().add(this);
    }

    public RelationPk getRelationPk() {
        return relationPk;
    }

    public void setRelationPk(RelationPk relationPk) {
        this.relationPk = relationPk;
    }

    public Long getTelegramUserId() {
        return relationPk.telegramUserId;
    }

    public void setTelegramUserId(Long telegramUserId) {
        this.relationPk.telegramUserId = telegramUserId;
    }

    public Long getTwitterUserId() {
        return relationPk.twitterUserId;
    }

    public void setTwitterUserId(Long twitterUserId) {
        this.relationPk.twitterUserId = twitterUserId;
    }

    public TelegramTwitterRelationType getType() {
        return relationPk.type;
    }

    public void setType(TelegramTwitterRelationType type) {
        this.relationPk.type = type;
    }

    public TelegramUser getTelegramUser() {
        return telegramUser;
    }

    public void setTelegramUser(TelegramUser telegramUser) {
        this.telegramUser = telegramUser;
    }

    @Override
    public String toString() {
        return "TelegramTwitterRelation{" +
                "relationPk=" + relationPk +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TelegramTwitterRelation that = (TelegramTwitterRelation) o;
        return Objects.equals(relationPk, that.relationPk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relationPk);
    }
}
