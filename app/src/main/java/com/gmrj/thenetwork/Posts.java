package com.gmrj.thenetwork;

public class Posts {

    public String uid,time,date,postimage,profileimage,fullname,description,title,description2;
     public  Posts(){

     }
    public Posts(String uid, String time, String date, String postimage, String profileimage, String fullname, String description,String description2,String title) {

        this.uid = uid;
        this.time = time;
        this.date = date;
        this.title=title;
        this.postimage = postimage;
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.description = description;
        this.description2 = description2;
    }

    public String getDescription2() {
        return description2;
    }

    public void setDescription2(String description2) {
        this.description2 = description2;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String tittle) {
        this.title = tittle;
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

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
