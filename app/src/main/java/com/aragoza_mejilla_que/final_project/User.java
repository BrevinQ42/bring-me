package com.aragoza_mejilla_que.final_project;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

    @PrimaryKey
    private String userID = UUID.randomUUID().toString();
    private String profilePicturePath;
    private String name;
    private String password;
    private Date lastLoginDate = new Date(System.currentTimeMillis());

    public User() {}

    public String getUserID() {
        return userID;
    }

    public void setUuid(String uuid) {
        userID = uuid;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", profilePicturePath='" + profilePicturePath + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", lastLoginDate=" + lastLoginDate +
                '}';
    }
}
