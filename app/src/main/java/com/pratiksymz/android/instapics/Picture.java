package com.pratiksymz.android.instapics;

public class Picture {
    private String username, title, description, imageUrl;

    public Picture() {
    }

    public Picture(String username, String title, String description, String imageUrl) {

        this.username = username;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(String image) {
        this.imageUrl = image;
    }

    public String getUsername() {
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return imageUrl;
    }
}
