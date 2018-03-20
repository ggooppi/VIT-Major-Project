package com.gopi.work;

/**
 * Created by gopinath.a on 17/3/18.
 */

public class Blog {

    private String title;
    private String Desc;
    private String image;
    private String username;
    private String userId;

    public Blog(){
    }

    public Blog(String title, String  image, String desc, String username, String userId){
        this.title = title;
        this.image = image;
        this.Desc = desc;
        this.username = username;
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        this.Desc = desc;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
