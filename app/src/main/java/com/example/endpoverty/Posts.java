package com.example.endpoverty;

public class Posts {

    public String uid, time, date, postImage, description, ProfileImage, fullname;

    public Posts()
    {

    }

    public Posts(String uid, String time, String date, String postImage, String description, String ProfileImage, String fullname) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postImage = postImage;
        this.description = description;
        this.ProfileImage = ProfileImage;
        this.fullname = fullname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getprofile_Image() {
        return ProfileImage;
    }

    public void setprofile_Image(String ProfileImage) {
        this.ProfileImage = ProfileImage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

}
