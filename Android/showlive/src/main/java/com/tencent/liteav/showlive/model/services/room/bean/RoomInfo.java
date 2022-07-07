package com.tencent.liteav.showlive.model.services.room.bean;

import java.util.Objects;

public class RoomInfo {
    public String roomId;
    public String roomName;
    public String ownerId;
    public String ownerName;
    public String streamUrl;
    public String coverUrl;
    public int    memberCount;
    public String ownerAvatar;
    public int    roomStatus;
    public int    totalJoined;

    public RoomInfo() {

    }

    public RoomInfo(String roomId, String roomName, String ownerId, String coverUrl) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.ownerId = ownerId;
        this.coverUrl = coverUrl;
    }

    @Override
    public String toString() {
        return "TXRoomInfo{"
                + "roomId='" + roomId + '\''
                + ", roomName='" + roomName + '\''
                + ", ownerId='" + ownerId + '\''
                + ", ownerName='" + ownerName + '\''
                + ", streamUrl='" + streamUrl + '\''
                + ", coverUrl='" + coverUrl + '\''
                + ", memberCount=" + memberCount
                + ", ownerAvatar='" + ownerAvatar + '\''
                + ", roomStatus=" + roomStatus + '\''
                + ", totalJoined='" + totalJoined
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoomInfo roomInfo = (RoomInfo) o;
        return Objects.equals(roomId, roomInfo.roomId)
                && Objects.equals(roomName, roomInfo.roomName)
                && Objects.equals(ownerId, roomInfo.ownerId)
                && Objects.equals(coverUrl, roomInfo.coverUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, roomName, ownerId, coverUrl);
    }
}
