package com.aragoza_mejilla_que.final_project;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Prompt extends RealmObject {
    @PrimaryKey
    private String promptID = UUID.randomUUID().toString();
    private String text;
    private String imagePath;
    private Date date;
    private Boolean isActive;

    public Prompt() {}

    public String getPromptID() {
        return promptID;
    }

    public void setPromptID(String promptID) {
        this.promptID = promptID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Prompt{" +
                "promptID='" + promptID + '\'' +
                ", text='" + text + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", date=" + date +
                ", isActive=" + isActive +
                '}';
    }
}