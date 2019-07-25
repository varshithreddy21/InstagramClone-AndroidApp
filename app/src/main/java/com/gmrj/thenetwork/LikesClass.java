package com.gmrj.thenetwork;

public class LikesClass {
    String fullname,profileimage;
    LikesClass(){

    }

    public LikesClass(String fullname, String profileimage) {
        this.fullname = fullname;
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}
