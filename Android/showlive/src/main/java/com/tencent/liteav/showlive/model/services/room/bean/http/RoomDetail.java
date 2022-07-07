package com.tencent.liteav.showlive.model.services.room.bean.http;

public class RoomDetail {
    public String roomId;
    public String title;
    public String category;
    public int    removed;
    public String createBy;
    public String updateBy;
    public String ownBy;
    public String createUtc;
    public String updateUtc;
    public int    status;
    public String cover;
    public String subject;
    public String description;
    public int    star;
    public int    nnUsers;
    public int    totalJoined;

    @Override
    public String toString() {
        return "RoomDetail{"
                + "roomId='" + roomId + '\''
                + ", title='" + title + '\''
                + ", category='" + category + '\''
                + ", removed='" + removed + '\''
                + ", createBy='" + createBy + '\''
                + ", createBy='" + createBy + '\''
                + ", updateBy='" + updateBy + '\''
                + ", ownBy=" + ownBy
                + ", createUtc='" + createUtc + '\''
                + ", updateUtc=" + updateUtc
                + ", status=" + status
                + ", cover=" + cover
                + ", subject=" + subject
                + ", description=" + description
                + ", star=" + star
                + ", nnUsers=" + nnUsers
                + ", totalJoined=" + totalJoined
                + '}';
    }
}
