package com.stupidtree.cloudliter.data.model;

import java.sql.Timestamp;
import java.util.Objects;

public class RelationEvent {
    public enum STATE {REQUESTING, ACCEPTED, REJECTED, DELETE}

    public enum ACTION {ACCEPT, REJECT}

    String id;
    String userId;
    String friendId;
    String otherId;
    String otherAvatar;
    String otherNickname;
    STATE state;
    boolean unread;
    Timestamp createdAt;
    Timestamp updatedAt;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getFriendId() {
        return friendId;
    }

    public String getOtherAvatar() {
        return otherAvatar;
    }

    public String getOtherNickname() {
        return otherNickname;
    }

    public STATE getState() {
        return state;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getOtherId() {
        return otherId;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }


    public boolean isUnread() {
        return unread;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationEvent that = (RelationEvent) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getFriendId(), that.getFriendId()) &&
                Objects.equals(getOtherAvatar(), that.getOtherAvatar()) &&
                Objects.equals(getOtherNickname(), that.getOtherNickname()) &&
                Objects.equals(isUnread(), that.isUnread()) &&
                getState() == that.getState();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUserId(), getFriendId(), getOtherAvatar(), getOtherNickname(), getState());
    }
}
