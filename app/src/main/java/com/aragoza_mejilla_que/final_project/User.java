package com.aragoza_mejilla_que.final_project;

import java.io.File;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

    @PrimaryKey
    private String userID = UUID.randomUUID().toString();
    private File profilePicturePath;
    private String name;
    private String password;

    public User() {}

    public String getUserID() {
        return userID;
    }

    public void setUuid(String uuid) {
        userID = uuid;
    }

    public File getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(File profilePicturePath) {
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

    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", profilePicturePath=" + profilePicturePath +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
