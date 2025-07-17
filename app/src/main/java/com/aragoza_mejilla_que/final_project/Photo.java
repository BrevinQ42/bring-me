package com.aragoza_mejilla_que.final_project;

import java.io.File;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Photo extends RealmObject {

    @PrimaryKey
    private String photoID = UUID.randomUUID().toString();
    private String imagePath;
    private String userID;
    private String promptID;
    private String caption;
    private Integer likeCount;

    public Photo() {}

    public String getPhotoID() {
        return photoID;
    }

    public void setPhotoID(String photoID) {
        this.photoID = photoID;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPromptID() {
        return promptID;
    }

    public void setPromptID(String promptID) {
        this.promptID = promptID;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "photoID='" + photoID + '\'' +
                ", imagePath=" + imagePath +
                ", userID='" + userID + '\'' +
                ", promptID='" + promptID + '\'' +
                ", caption='" + caption + '\'' +
                ", likeCount=" + likeCount +
                '}';
    }
}
